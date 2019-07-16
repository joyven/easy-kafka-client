package com.zhoujunwen.example.listener;

import com.zhoujunwen.consumer.listener.ConsumerStartListener;
import lombok.extern.slf4j.Slf4j;

/**
 * DefaultStartListener
 *
 * @author zhoujunwen
 * @date 2019-07-12
 * @time 15:15
 * @desc
 */
@Slf4j
public class DefaultStartListener implements ConsumerStartListener {
    @Override
    public void onStartSuccess() {
        log.info("kafka消费者线程开启成功");
    }

    @Override
    public void onStartFailed(Throwable e) {
        log.info("kafka消费者线程开启失败", e);

    }
}
