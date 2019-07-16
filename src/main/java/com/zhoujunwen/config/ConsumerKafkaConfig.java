package com.zhoujunwen.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * ConsumerKafkaConfig
 *
 * @author zhoujunwen
 * @date 2019-07-11
 * @time 14:49
 * @desc
 */
@Getter
@Setter
public class ConsumerKafkaConfig {
    private String bootstrapServers;
    private String groupId;
    private String clientId;
    private String topicPrefix = "";
    private List<String> topics = new ArrayList<>();
    private int connectSessionTimeout = 10000;
    private int commitInterval = 1000;
    private int threadCount = 10;
    private boolean manualCommit = false;
}
