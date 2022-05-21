package com.jufeng.distributed.box.mysql.lock.support;

/**
 * @program: distributed-box
 * @description: 锁接口
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-21 16:36
 **/
public interface Lock {

    /**
     * 阻塞获取锁
     */
    void lock(String resource);

    /**
     * 非阻塞获取锁
     * @return
     */
    boolean nonLock(String resource);

    boolean nonLock(String resource,long timeout);

    /**
     * 释放锁
     * @return
     */
    boolean unLock(String resource);
}
