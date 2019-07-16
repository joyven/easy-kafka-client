package com.zhoujunwen.consumer;

import com.zhoujunwen.consumer.listener.ConsumerStartListener;
import com.zhoujunwen.consumer.listener.ConsumerStopListener;

/**
 * Consumer
 *
 * @author zhoujunwen
 * @date 2019-07-11
 * @time 11:35
 * @desc
 */
public interface Consumer {
    /**
     * 开启消费者
     *
     * @param listener 消费者启动时的回调监听器
     */
    void start(ConsumerStartListener listener);

    /**
     * 停止消费者
     *
     * @param listener 消费者停止时的回调监听器
     */
    void stop(ConsumerStopListener listener);
}
