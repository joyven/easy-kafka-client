package com.zhoujunwen.config;

import lombok.Data;

/**
 * KafkaConfig
 *
 * @author zhoujunwen
 * @date 2019-07-11
 * @time 10:34
 * @desc
 */
@Data
public class KafkaConfig {
    private String bootstrapServers;
    private String topicPrefix = "";
    private String producerType = "async";
    private Integer sendTimeoutMs;

    private String compressionType = "gzip";
    private int batchSize = 32768;
    private int bufferMemory = 104857600; //100MB
    private int lingerMs = 50;

    /**
     * 推特雪花算法key生成，workerId和dataCenterId均为0~31
     */
    private long workerId = 0;
    private long dataCenterId = 0;
}
