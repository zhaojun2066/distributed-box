package com.jufeng.distributed.box.mysql.lock.service;


import com.jufeng.distributed.box.mysql.lock.entity.UniqueLock;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-21 16:34
 **/
public interface UniqueLockService {


    boolean insert(UniqueLock uniqueLock);

    UniqueLock findByResource(String resource);

    boolean incCount(Integer id);

    boolean deCount(Integer id);

    long  count(String resource);

    boolean deleteById(Integer id);
}
