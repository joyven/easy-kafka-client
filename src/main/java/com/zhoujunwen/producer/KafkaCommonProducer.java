package com.zhoujunwen.producer;

import com.zhoujunwen.config.KafkaConfig;
import com.zhoujunwen.utils.SnowflakeIdWorker;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * KafkaCommonProducer
 *
 * @author zhoujunwen
 * @date 2019-07-10
 * @time 17:54
 * @desc
 */
@Slf4j
public class KafkaCommonProducer implements Producer {
    private final String UTF_8 = "UTF-8";
    private org.apache.kafka.clients.producer.Producer<String, byte[]> producer;
    private SnowflakeIdWorker idWorker0;
    private KafkaProducerCallback defaultCallback = new DefaultProducerCallBack();

    @Getter
    @Setter
    @Resource
    private KafkaConfig kafkaConfig;

    @PostConstruct
    public void init() {
        Properties prop = initConfig();
        producer = new KafkaProducer<>(prop);
        idWorker0 = new SnowflakeIdWorker(kafkaConfig.getWorkerId(), kafkaConfig.getDataCenterId());
    }

    @PreDestroy
    public void destroy() {
        if (null != producer) producer.close();
    }

    @Override
    public void produce(String topic, String message) {
        String key = Long.toString(idWorker0.nextId());
        this.produce(topic, key, message);
    }

    @Override
    public void produce(final String topic, final String key, final String message) {
        this.produce(topic, key, message, defaultCallback);
    }

    @Override
    public void produce(final String topic, final String key, final String message,
                        final KafkaProducerCallback callback) {
        String prefix = kafkaConfig.getTopicPrefix();
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(prefix + topic, key,
                message.getBytes(Charset.forName(UTF_8)));
        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                callback.onSuccess(topic, key, message);
            } else {
                callback.onError(topic, key, message, exception);
            }
        });
    }

    /**
     * 初始化kafka配置
     *
     * @return 返回配置属性
     */
    private Properties initConfig() {
        Properties prop = new Properties();
        // kafka broker list
        prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        // set ack
        prop.put(ProducerConfig.ACKS_CONFIG, "1");
        // set retries
        prop.put(ProducerConfig.RETRIES_CONFIG, "2");
        // set batch size
        prop.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaConfig.getBatchSize());
        // compressionType type
        prop.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaConfig.getCompressionType());
        // lingerMs ms
        prop.put(ProducerConfig.LINGER_MS_CONFIG, kafkaConfig.getLingerMs());
        // buffer memory
        prop.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaConfig.getBufferMemory());
        // max request size 100MB
        prop.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 100 * 1024 * 1024);
        // send request timeout
        prop.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaConfig.getSendTimeoutMs());
        // set serialize class
        prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        return prop;
    }
}
