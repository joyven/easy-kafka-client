package com.zhoujunwen.consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * Message
 *
 * @author zhoujunwen
 * @date 2019-07-11
 * @time 14:24
 * @desc
 */
@AllArgsConstructor
@Getter
public class Message<T> implements Serializable {
    private final String key;
    private final T value;
}
