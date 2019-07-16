package com.zhoujunwen.consumer.processor;

import com.zhoujunwen.consumer.Message;

import java.util.Collection;

/**
 * BulkMessageProcessor
 *
 * @author zhoujunwen
 * @date 2019-07-11
 * @time 14:22
 * @desc
 */
public interface BulkMessageProcessor {
    void onMessage(Collection<Message<byte[]>> messages);
}
