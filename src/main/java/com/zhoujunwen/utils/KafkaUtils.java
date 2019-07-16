package com.zhoujunwen.utils;

import kafka.admin.AdminUtils;
import kafka.admin.BrokerMetadata;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;
import org.apache.kafka.common.security.JaasUtils;
import scala.collection.Map;
import scala.collection.Seq;

import java.util.Properties;

/**
 * KafkaUtils
 *
 * @author zhoujunwen
 * @date 2019-07-09
 * @time 17:47
 * @desc
 */
public class KafkaUtils {
    /**
     * 连接 Zk
     */
    private static final String ZK_CONNECT = "server-1:2181,server-2:2181,server-3:2181";
    /**
     * session 过期时间
     */
    private static final int SESSION_TIMEOUT = 30000;
    /**
     * 连接超时时间
     */
    private static final int CONNECT_TIMEOUT = 30000;

    /**
     * 创建TOPIC
     *
     * @param topic      主题
     * @param partition  分区
     * @param replica    副本
     * @param properties 属性
     */
    public static void createTopic(String topic, int partition, int replica, Properties properties) {
        ZkUtils zkUtils = null;
        try {
            zkUtils = ZkUtils.apply(ZK_CONNECT, SESSION_TIMEOUT, CONNECT_TIMEOUT, JaasUtils.isZkSecurityEnabled());
            if (!AdminUtils.topicExists(zkUtils, topic)) {
                AdminUtils.createTopic(zkUtils, topic, partition, replica, properties,
                        AdminUtils.createTopic$default$6());
            } else {
                System.out.println("zkUtils...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != zkUtils) zkUtils.close();
        }
    }

    /**
     * 修改主题级别的配置
     *
     * @param topic      主题
     * @param properties 配置属性
     */
    public static void modifyTopicConfig(String topic, Properties properties) {
        ZkUtils zkUtils = null;
        try {
            // 实例化zk工具类
            zkUtils = ZkUtils.apply(ZK_CONNECT, SESSION_TIMEOUT, CONNECT_TIMEOUT,
                    JaasUtils.isZkSecurityEnabled());
            Properties curProp = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), topic);
            curProp.putAll(properties);

            AdminUtils.changeTopicConfig(zkUtils, topic, curProp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != zkUtils) zkUtils.close();
        }
    }

    /**
     * 修改分区和副本
     *
     * @param topic                主题
     * @param numPartitions        分区总数，例如：2
     * @param replicaAssignmentStr 副本配置，例如："3:1,2:2",表示两个分区各有两个副本，且副本分别对应的brokerId。
     * @param checkBrokerAvailable 是否检测Broker可用
     */
    public static void modifyPartitions(String topic, int numPartitions, String replicaAssignmentStr, boolean checkBrokerAvailable) {
        ZkUtils zkUtils = null;
        try {
            zkUtils = zkUtils.apply(ZK_CONNECT, SESSION_TIMEOUT, CONNECT_TIMEOUT,
                    JaasUtils.isZkSecurityEnabled());
            AdminUtils.addPartitions(zkUtils, topic, numPartitions, replicaAssignmentStr, checkBrokerAvailable,
                    AdminUtils.addPartitions$default$6());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != zkUtils) zkUtils.close();
        }
    }

    /**
     * 修改分区副本
     *
     * @param topic         主题
     * @param numPartitions 分区总数
     * @param replicas      副本数
     */
    public static void modifyReplicas(String topic, int numPartitions, int replicas) {
        ZkUtils zkUtils = null;
        try {
            // 实例化zkUtils
            zkUtils = ZkUtils.apply(ZK_CONNECT, SESSION_TIMEOUT, CONNECT_TIMEOUT,
                    JaasUtils.isZkSecurityEnabled());
            // 获取代理元数据信息
            Seq<BrokerMetadata> brokerMetadataSeq = AdminUtils.getBrokerMetadatas(zkUtils,
                    AdminUtils.getBrokerMetadatas$default$2(), AdminUtils.getBrokerMetadatas$default$3());
            // 生成分区副本方案：numPartitions 个分区，replicas 个副本
            Map<Object, Seq<Object>> replicaAssignment = AdminUtils.assignReplicasToBrokers(brokerMetadataSeq,
                    numPartitions, replicas,
                    AdminUtils.assignReplicasToBrokers$default$4(),
                    AdminUtils.assignReplicasToBrokers$default$5());
            // 修改分区副本分配方案
            AdminUtils.createOrUpdateTopicPartitionAssignmentPathInZK(zkUtils, topic, replicaAssignment, null, true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != zkUtils) zkUtils.close();
        }
    }

    /**
     * 删除主题
     *
     * @param topic 主题
     */
    public static void deleteTopic(String topic) {
        ZkUtils zkUtils = null;
        try {
            // 实例化zkUtils
            zkUtils = ZkUtils.apply(ZK_CONNECT, SESSION_TIMEOUT, CONNECT_TIMEOUT,
                    JaasUtils.isZkSecurityEnabled());
            AdminUtils.deleteTopic(zkUtils, topic);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != zkUtils) zkUtils.close();
        }
    }
}
