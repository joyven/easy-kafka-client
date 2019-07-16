package com.zhoujunwen.consumer;

import com.zhoujunwen.config.ConsumerKafkaConfig;
import com.zhoujunwen.consumer.listener.ConsumerStartListener;
import com.zhoujunwen.consumer.listener.ConsumerStopListener;
import com.zhoujunwen.consumer.processor.BulkMessageProcessor;
import com.zhoujunwen.exception.StopImmediately;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * AbstractBulkKafkaConsumer
 *
 * @author zhoujunwen
 * @date 2019-07-11
 * @time 16:23
 * @desc
 */
@Slf4j
public abstract class AbstractBulkKafkaConsumer implements Consumer, BulkMessageProcessor {
    @Getter
    @Setter
    @Resource
    private ConsumerKafkaConfig consumerKafkaConfig;
    private transient volatile boolean started;
    private volatile List<ConsumerThread> consumerThreads = new ArrayList<>();

    {
        reset();
    }

    @Override
    public void start(ConsumerStartListener listener) {
        try {
            connectionKafka();
        } catch (Exception e) {
            shutdownNoException();
            listener.onStartFailed(e);
            return;
        }
        started = Boolean.TRUE;
        listener.onStartSuccess();
    }

    @Override
    public void stop(ConsumerStopListener listener) {
        if (!started) {
            listener.onStopSuccess();
            return;
        }
        shutdownNoException();
        started = Boolean.FALSE;
        listener.onStopSuccess();
    }

    private void connectionKafka() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerKafkaConfig.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerKafkaConfig.getGroupId());
//        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerKafkaConfig.getClientId());
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");

        properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 1024 * 1024 * 100); // 设置一次 fetch 请求取得的数据最大值为100M,默认是5MB
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        doHandler(properties);
    }

    private void doHandler(Properties properties) {
        for (int i = 0; i < consumerKafkaConfig.getThreadCount(); i++) {
            for (String topic : getKafkaTopicWithPrefix()) {
                KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties);
                // 订阅主题
                log.info("订阅主题:{}", topic);
                consumer.subscribe(Collections.singletonList(topic));
                createConsumerThread(consumerKafkaConfig, consumer, topic, this, i);
            }
        }
    }

    private List<String> getKafkaTopicWithPrefix() {
        String prefix = consumerKafkaConfig.getTopicPrefix();
        return consumerKafkaConfig.getTopics().stream().map(t -> prefix + t).collect(Collectors.toList());
    }

    private synchronized void shutdownNoException() {
        // 退出各线程
        for (ConsumerThread thread : consumerThreads) {
            thread.shutdown();
        }
        // 重置状态
        reset();
    }

    private void reset() {
        consumerThreads.clear();
    }

    private void createConsumerThread(ConsumerKafkaConfig config, KafkaConsumer<String, byte[]> consumer, String topic,
                                      BulkMessageProcessor processor, int id) {
        String threadName = String.format("Worker-%s-%s-%s", topic, config.getGroupId(), id);
        ConsumerThread thread = new ConsumerThread(consumer, processor, consumerKafkaConfig);
        thread.setName(threadName);
        thread.start();
        consumerThreads.add(thread);
    }


    //    @Slf4j
    class ConsumerThread extends Thread {
        private KafkaConsumer<String, byte[]> consumer;
        private BulkMessageProcessor processor;
        private ConsumerKafkaConfig consumerKafkaConfig;
        private long lastCommitTime;
        private final AtomicBoolean isRunning = new AtomicBoolean(false);
        private final AtomicBoolean messageConsumedAfterLastCommit = new AtomicBoolean(false);


        ConsumerThread(KafkaConsumer<String, byte[]> consumer, BulkMessageProcessor processor,
                       ConsumerKafkaConfig consumerKafkaConfig) {
            this.consumer = consumer;
            this.processor = processor;
            this.consumerKafkaConfig = consumerKafkaConfig;
        }

        @Override
        public void run() {
            isRunning.set(true);
            while (isRunning.get()) {
                try {
                    log.info("one run");
                    oneRun(consumer, consumerKafkaConfig);
                } catch (RuntimeException ex) {
                    log.warn("Stop immediately without commit");
                    return;
                }
                System.out.println("11111");
            }
        }

        void shutdown() {
            isRunning.set(false);

            try {
                this.join(60000);
            } catch (InterruptedException e) {
            }

            try {
                consumer.close();
            } catch (Exception e) {
                log.error("Failed to shutdown consumer", e);
            }
        }

        private void oneRun(KafkaConsumer<String, byte[]> consumer, ConsumerKafkaConfig config) throws StopImmediately {
            try {
                oneRunException(consumer, config);
            } catch (StopImmediately ex) {
                throw ex;
            } catch (Exception exc) {
                log.error("Exception on consumer thread", exc);
                // 避免死循环导致狂飙CPU
                mySleep(1000);
            }
        }

        private void oneRunException(KafkaConsumer<String, byte[]> consumer, ConsumerKafkaConfig config) {
            while (true) {
                ConsumerRecords<String, byte[]> records = consumer.poll(1000);
                List<Message<byte[]>> messages = new ArrayList<>(records.count());
                for (ConsumerRecord<String, byte[]> record : records) {
                    Message<byte[]> message = new Message<>(record.key(), record.value());
                    messages.add(message);
                }
                if (messages.size() != 0) {
                    processor.onMessage(messages);
                    messageConsumedAfterLastCommit.set(true);
                    if (config.isManualCommit()) {
                        commitWhenNecessary(consumer, config);
                    }
                }
            }
        }


        private void commitWhenNecessary(KafkaConsumer<String, byte[]> consumer, ConsumerKafkaConfig config) {
            // 上次提交之后消息没变化, 没必要提交了
            if (!messageConsumedAfterLastCommit.get()) {
                return;
            }

            // 没必要太频繁的提交
            if (time() - lastCommitTime < config.getCommitInterval()) {
                return;
            }

            // 提交了
            commit(consumer);
            lastCommitTime = time();
            messageConsumedAfterLastCommit.set(false);
        }

        private void commit(KafkaConsumer<String, byte[]> consumer) {
            try {
                consumer.commitAsync((offsets, exception) -> {
                    if (null == exception) {
                        log.debug("批量提交成功: {}", offsets.size());
                    } else {
                        log.error("批量提交失败：{}", offsets.size(), exception);
                    }
                });
            } catch (Exception ex) {
                log.error("Failed to commit offset for kafka", ex);
            }
        }

        private long time() {
            return System.currentTimeMillis();
        }

        private void mySleep(long ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
            }
        }
    }
}
