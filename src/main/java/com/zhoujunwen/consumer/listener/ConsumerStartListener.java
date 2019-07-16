package com.zhoujunwen.consumer.listener;

/**
 * ConsumerStartListener
 *
 * @author zhoujunwen
 * @date 2019-07-11
 * @time 14:19
 * @desc
 */
public interface ConsumerStartListener {
    void onStartSuccess();

    void onStartFailed(Throwable e);
}
