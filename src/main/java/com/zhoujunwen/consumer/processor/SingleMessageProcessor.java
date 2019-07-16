package com.zhoujunwen.consumer.processor;


/**
 * SingleMessageProcessor
 *
 * @author zhoujunwen
 * @date 2019-07-11
 * @time 14:23
 * @desc
 */
public interface SingleMessageProcessor {
    void onMessage(byte[] message);
}
