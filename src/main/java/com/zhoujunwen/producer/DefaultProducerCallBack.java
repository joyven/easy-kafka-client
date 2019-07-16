package com.zhoujunwen.producer;

import lombok.extern.slf4j.Slf4j;

/**
 * DefaultProducerCallBack
 *
 * @author zhoujunwen
 * @date 2019-07-11
 * @time 11:08
 * @desc
 */
@Slf4j
public class DefaultProducerCallBack implements KafkaProducerCallback {
    @Override
    public void onError(String topic, String key, String message, Exception exception) {
        log.error(String.format("Failed to send message, topic=%s, key=%s, message=%s", topic, key, message), exception);
    }

    @Override
    public void onSuccess(String topic, String key, String message) {
        log.info(String.format("Success to send message, topic=%s, key=%s, message=%s", topic, key, message));
    }
}
