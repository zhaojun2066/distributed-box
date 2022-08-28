package com.jufeng.distributed.box.lock.zookeeper.common;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-25 22:08
 **/
public class LockException extends Exception {

    public LockException(String message) {
        super(message);
    }
}
