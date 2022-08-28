package com.jufeng.distributed.box.lock.zookeeper.support;

import com.jufeng.distributed.box.lock.zookeeper.common.LockException;

/**
 * @program: distributed-box
 * @description: 锁接口
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-24 21:57
 **/
public interface Lock {

    /**
     * 阻塞获取锁
     * @return
     */
    void lock(String source) throws LockException;

    /**
     * 非阻塞获取锁
     * @return
     */
    boolean nonLock(String source,int retries);

    /**
     * 异步通知
     * @param source
     * @return
     */
    void asynchronousLock(String source,Runnable runnable);

    boolean nonLock(String source,long timeout);


    boolean unLock();
}
