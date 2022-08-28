package com.jufeng.distributed.box.lock.zookeeper.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-24 21:46
 **/

@Data
@NoArgsConstructor
@ConfigurationProperties("lock-zk")
public class ZKProperties {
    private String servers = "127.0.0.1:2181";
    private int baseSleepTimeMs = 1000;
    private int maxRetries = 5;
    private int sessionTimeoutMs = 85000;
    private String namespace = "lock";
    private String baseLockPath = "/distribute-lock";
}
