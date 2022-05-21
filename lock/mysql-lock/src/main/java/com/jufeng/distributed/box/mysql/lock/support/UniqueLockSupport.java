package com.jufeng.distributed.box.mysql.lock.support;

import com.jufeng.distributed.box.mysql.lock.entity.UniqueLock;
import com.jufeng.distributed.box.mysql.lock.service.UniqueLockService;
import com.jufeng.distributed.box.mysql.lock.util.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.locks.LockSupport;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-21 16:42
 **/
@Slf4j
@Component
public class UniqueLockSupport implements Lock {

    private static final long WAIT_TIME = 5*1000*1000;
    @Autowired
    private UniqueLockService uniqueLockService;

    @Override
    public void lock(String resource) {
        // 这里通过一个死循环，来模拟了阻塞的实现
        while (!lockResource(resource)){
            //等待一会再去获取
            log.info("resource: {}, owner: {} , 获取锁失败",resource,LockUtil.getLockOwner());
            LockSupport.parkNanos(WAIT_TIME);
        }
        log.info("resource: {}, owner: {} , 获取锁成功",resource,LockUtil.getLockOwner());
    }

    @Override
    public boolean nonLock(String resource) {
        return lockResource(resource);
    }

    @Override
    public boolean nonLock(String resource,long timeout) {
        long lastTime = System.currentTimeMillis() + timeout;
        while (!lockResource(resource)){
            if (System.currentTimeMillis() > lastTime) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean unLock(String resource) {
        //这里要注意，释放锁，要保证是自己的锁才可以
        final UniqueLock uniqueLock = uniqueLockService.findByResource(resource);
        if (uniqueLock == null) return false;
        // 是自己的锁，在进行操作
        if (Objects.equals(uniqueLock.getLockOwner(),LockUtil.getLockOwner())){
            // 如果大于0 说明count 要减1
            if (uniqueLock.getLockCount() > 1){
                log.info("resource: {}, owner: {} , 释放锁-1",resource,LockUtil.getLockOwner());
              return   uniqueLockService.deCount(uniqueLock.getId());
            }else {
                // ==0 ，删除记录就可以了
                log.info("resource: {}, owner: {} , 释放锁",resource,LockUtil.getLockOwner());
              return   uniqueLockService.deleteById(uniqueLock.getId());
            }
        }
        log.info("resource: {}, owner: {} , 释放锁",resource,LockUtil.getLockOwner());
        return false;
    }

    private boolean lockResource(String resource){
        //这里先检查下 是否有这个记录
        UniqueLock uniqueLock = uniqueLockService.findByResource(resource);
        // 如果是null ，则尝试插入数据
        if (uniqueLock == null){
            // 如果唯一索引冲突，那么应该在这里捕获到，返回获取锁失败
           try {
               uniqueLock = UniqueLock.builder()
                       .lockResource(resource)
                       .lockCount(1)
                       .lockDesc("")
                       .lockOwner(LockUtil.getLockOwner())
                       .createTime(LocalDateTime.now())
                       .updateTime(LocalDateTime.now()).build();
               return uniqueLockService.insert(uniqueLock);
           }catch (Exception e){
              //  e.printStackTrace();
               return false;
           }
        }

        //是否重入
        if (Objects.equals(uniqueLock.getLockOwner(),LockUtil.getLockOwner())){
            log.info("resource: {}, owner: {} , 重入获锁",resource,LockUtil.getLockOwner());
           return uniqueLockService.incCount(uniqueLock.getId());
        }
        return false;

    }
}
