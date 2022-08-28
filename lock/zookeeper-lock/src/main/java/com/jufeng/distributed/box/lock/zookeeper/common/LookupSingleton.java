package com.jufeng.distributed.box.lock.zookeeper.common;

import com.jufeng.distributed.box.lock.zookeeper.support.ZookeeperLock;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-26 22:53
 **/

@Component
public abstract class LookupSingleton {
    @Lookup
    public abstract ZookeeperLock zookeeperLock ();
}
