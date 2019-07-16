package com.zhoujunwen;

import com.google.common.collect.Lists;
import com.zhoujunwen.config.ConsumerKafkaConfig;
import com.zhoujunwen.consumer.Consumer;
import com.zhoujunwen.example.consumer.DefaultBulkKafkaConsumer;
import com.zhoujunwen.example.listener.DefaultStartListener;

/**
 * ConsumerTest
 *
 * @author zhoujunwen
 * @date 2019-07-12
 * @time 15:17
 * @desc
 */
public class ConsumerTest {
    private static ConsumerKafkaConfig config;

    static {
        config = new ConsumerKafkaConfig();
        config.setBootstrapServers("localhost:9092,localhost:9093,localhost:9094,localhost:9095");
        config.setGroupId("kafka-demo-consumer");
        config.setThreadCount(1);
        config.setTopics(Lists.newArrayList("kafka-demo-consumer-test"));

    }

    public static void main(String[] args) {
        Consumer consumer = new DefaultBulkKafkaConsumer();
        ((DefaultBulkKafkaConsumer) consumer).setConsumerKafkaConfig(config);
        consumer.start(new DefaultStartListener());
    }
}
