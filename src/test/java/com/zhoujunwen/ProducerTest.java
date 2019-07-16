package com.zhoujunwen;

import com.zhoujunwen.config.KafkaConfig;
import com.zhoujunwen.producer.KafkaCommonProducer;
import com.zhoujunwen.producer.Producer;

/**
 * ProducerTest
 *
 * @author zhoujunwen
 * @date 2019-07-12
 * @time 16:10
 * @desc
 */
public class ProducerTest {
    private static KafkaConfig kafkaConfig;

    static {
        kafkaConfig = new KafkaConfig();
        kafkaConfig.setBootstrapServers("localhost:9092,localhost:9093,localhost:9094,localhost:9095");
        kafkaConfig.setSendTimeoutMs(10000);
    }

    public static void main(String[] args) {
        Producer producer = new KafkaCommonProducer();
        ((KafkaCommonProducer) producer).setKafkaConfig(kafkaConfig);
        ((KafkaCommonProducer) producer).init();
        for (long i = 0; ; i++) {
            String topic = "kafka-demo-consumer-test";
            String message = "zjw测试数据";
            producer.produce(topic, message + i);
        }

    }
}
