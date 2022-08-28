package com.jufeng.distributed.box.lock.zookeeper.common;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-24 21:47
 **/
@Configuration
@EnableConfigurationProperties(ZKProperties.class)
public class ZookeeperConfiguration {


    @Bean
    public CuratorFramework curatorFramework(ZKProperties zkProperties){
        //创建重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(zkProperties.getBaseSleepTimeMs(), zkProperties.getMaxRetries());

        //创建zookeeper客户端
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString(zkProperties.getServers())
                .sessionTimeoutMs(zkProperties.getSessionTimeoutMs())
                .retryPolicy(retryPolicy)
                .namespace(zkProperties.getNamespace())
                .build();

        client.start();
//        try {
//            if (client.checkExists().forPath(zkProperties.getBaseLockPath()) == null){
//                client.create().creatingParentContainersIfNeeded()
//                        .withMode(CreateMode.PERSISTENT)
//                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
//                        .forPath(zkProperties.getBaseLockPath());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return client;
    }
}
