package com.jufeng.distributed.box.mysql.lock.util;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-21 17:13
 **/
public class LockUtil {

    public static String getLockOwner(){
        //todo: ip + 线程id，这里方便 ，我直接获取了线程id
        return String.valueOf(Thread.currentThread().getId());

    }
}
