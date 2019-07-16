package com.zhoujunwen.producer;

/**
 * Producer
 *
 * @author zhoujunwen
 * @date 2019-07-10
 * @time 18:01
 * @desc
 */
public interface Producer {
    void produce(String topic, String message);

    void produce(String topic, String key, String message);

    void produce(String topic, String key, String message, KafkaProducerCallback callback);
}
