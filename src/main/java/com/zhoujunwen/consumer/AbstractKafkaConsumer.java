package com.zhoujunwen.consumer;

import com.zhoujunwen.config.ConsumerKafkaConfig;
import com.zhoujunwen.consumer.listener.ConsumerStartListener;
import com.zhoujunwen.consumer.listener.ConsumerStopListener;
import com.zhoujunwen.consumer.processor.SingleMessageProcessor;
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
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * AbstractKafkaConsumer
 *
 * @author zhoujunwen
 * @date 2019-07-11
 * @time 11:35
 * @desc
 */
@Slf4j
public abstract class AbstractKafkaConsumer implements Consumer, SingleMessageProcessor {

    @Getter
    @Setter
    @Resource
    private ConsumerKafkaConfig consumerKafkaConfig;
    private transient volatile boolean started;
    private KafkaConsumer<String, byte[]> consumer;

    @Override
    public void start(ConsumerStartListener listener) {
        try {
            connectionKafka();
        } catch (Exception e) {
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
        consumer.close();
        started = Boolean.FALSE;
        listener.onStopSuccess();
    }

    private void connectionKafka() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerKafkaConfig.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerKafkaConfig.getGroupId());
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerKafkaConfig.getClientId());
        properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 1024 * 1024 * 100); // 设置一次 fetch 请求取得的数据最大值为100M,默认是5MB
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        doHandler(properties);
    }

    private List<String> getKafkaTopicWithPrefix() {
        String prefix = consumerKafkaConfig.getTopicPrefix();
        return consumerKafkaConfig.getTopics().stream().map(t -> prefix + t).collect(Collectors.toList());
    }

    private void doHandler(Properties properties) {
        consumer = new KafkaConsumer<>(properties);
        // 订阅主题
        consumer.subscribe(getKafkaTopicWithPrefix());
        try {
            int minCommitSize = 10;
            int icount = 0;
            while (true) {
                ConsumerRecords<String, byte[]> records = consumer.poll(consumerKafkaConfig.getConnectSessionTimeout());
                for (ConsumerRecord<String, byte[]> record : records) {
                    onMessage(record.value());
                    icount++;
                }

                if (icount >= minCommitSize) {
                    consumer.commitAsync((offsets, exception) -> {
                        if (null == exception) {
                            // 表示偏移量提交成功
                            log.debug("手动提交偏移量提交成功:{}", offsets.size());
                        } else {
                            // 表示提交偏移量发生了异常，根据业务进行相关处理
                            log.error("手动提交偏移量发生了异常:{}", offsets.size());
                        }
                    });
                    icount = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
