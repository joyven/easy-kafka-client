package com.zhoujunwen.example.consumer;

import com.zhoujunwen.consumer.AbstractBulkKafkaConsumer;
import com.zhoujunwen.consumer.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * DefaultBulkKafkaConsumer
 *
 * @author zhoujunwen
 * @date 2019-07-12
 * @time 15:12
 * @desc
 */
@Slf4j
public class DefaultBulkKafkaConsumer extends AbstractBulkKafkaConsumer {
    @Override
    public void onMessage(Collection<Message<byte[]>> messages) {
        for (Message<byte[]> message : messages) {
            try {
                log.info("message:key={},value={}", message.getKey(), new String(message.getValue(), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
