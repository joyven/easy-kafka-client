package com.zhoujunwen.producer;

/**
 * KafkaProducerCallback
 *
 * @author zhoujunwen
 * @date 2019-07-10
 * @time 18:01
 * @desc
 */
public interface KafkaProducerCallback {
    void onError(String topic, String key, String message, Exception exception);

    void onSuccess(String topic, String key, String message);
}
