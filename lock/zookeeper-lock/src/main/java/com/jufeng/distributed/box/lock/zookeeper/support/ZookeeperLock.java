package com.jufeng.distributed.box.lock.zookeeper.support;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.jufeng.distributed.box.lock.zookeeper.common.CommonUtil;
import com.jufeng.distributed.box.lock.zookeeper.common.LockException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-24 22:01
 **/
@Slf4j
@RequiredArgsConstructor
@Component
@Scope(value = "prototype")
public class ZookeeperLock implements Lock {

    private final CuratorFramework curatorFramework;



    private final Watcher watcher = event -> {
        if (event.getType() == Watcher.Event.EventType.NodeDeleted){
            notifyAllFromWatcher();
        }
    };

    /**
     * key: lock path
     * value： 重入的次数
     */
    private static final TransmittableThreadLocal<LockInfo> THREAD_LOCAL = new TransmittableThreadLocal<>();

//    private void setNotify(){
//        final LockInfo lockInfo = getLocalMap();
//        lockInfo.notify = true;
//    }
//
//    private boolean isNotify(){
//        return getLocalMap().notify;
//    }

    private LockInfo getLocalMap(){
        LockInfo lockInfo = THREAD_LOCAL.get();
        if (lockInfo == null){
            lockInfo = new LockInfo();
            THREAD_LOCAL.set(lockInfo);
        }
        return lockInfo;
    }

    private String createLockPath(String source)  {
        String base = "/" + source;
        // 创建临时节点,这里肯定谁最小是谁先创建出来
        String currentPath = null;
        try {
            currentPath = curatorFramework.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(base+"/lock_");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentPath;
    }

    private String getLockPath(){
        final LockInfo lockInfo = getLocalMap();
        return lockInfo.lockPath;
    }
    private String getBasePath(){
        final LockInfo lockInfo = getLocalMap();
        return "/"+lockInfo.source;
    }
    private String getSource(){
        final LockInfo lockInfo = getLocalMap();
        return lockInfo.source;
    }
    private void setLock(){
        final LockInfo lockInfo = getLocalMap();
        lockInfo.lock = true;
    }

    private void incCount(){
        final LockInfo lockInfo = getLocalMap();
        lockInfo.count++;
    }

    private void deCount(){
        final LockInfo lockInfo = getLocalMap();
        lockInfo.count--;
    }

    private int getCount(){
        return getLocalMap().count;
    }

    private boolean isLock(){
        return  getLocalMap().lock;
    }

    private synchronized void notifyAllFromWatcher(){
        notifyAll();
    }

    private  void nextLock() throws LockException {
        boolean deleted = false;
        try {
            // 不相等，那么说明有比它大的，那么找出它的弟弟节点，进行监听
            //监听上一个节点
            final List<String> childrenPath = curatorFramework.getChildren().forPath(getBasePath());
            final String youngerBrother = CommonUtil.getYoungerBrother(getSource(), childrenPath, getLockPath());
            //如果为空个，说明就剩下她自己一个了，那么直接返回获取
            if (StringUtils.isEmpty(youngerBrother)){
                log.error("currentThread=> "+Thread.currentThread().getId()+"'s youngerBrother is null ");
                lock(getSource());
                return;
            }
            curatorFramework.getData().usingWatcher(watcher).forPath(youngerBrother);
            synchronized(this){
                wait();
            }

            lock(getSource());
        }catch (Exception e){

            //如果是 NoNodeException ，说明我监听的节点不存了，那么如要继续获取锁
            if(e instanceof KeeperException.NoNodeException){
                lock(getSource());
                return;
            }
            e.printStackTrace();
            deleted = true;
            throw new LockException("获取锁失败：" + e.getMessage());
        }finally {
            // 等待超时，没有获取到锁，那么删除zookeeper 中的临时节点和thread local内的数据
            if (deleted){
                removeResource();
                throw new LockException("超时-获取锁失败");
            }
        }

    }

    private void initCurrentLock(String source) throws LockException {
        final LockInfo lockInfo = getLocalMap();
        //如果为空，那么说第一次尝试获取锁
        if (StringUtils.isEmpty(lockInfo.lockPath)){
            String lockPath = createLockPath(source);
            if (StringUtils.isEmpty(lockPath)){
                throw new LockException("创建锁失败，请稍后重试");
            }
            lockInfo.source = source;
            lockInfo.lockPath = lockPath;
        }
    }

    @Override
    public void lock(String source) throws LockException {

        initCurrentLock(source);

       //如果获得了，那么不要继续了
        if (lockResource()){
            return;
        }
        try {
            //阻塞 监听
            nextLock();
        } catch (Exception e) {
            e.printStackTrace();
            throw  new LockException("获取锁失败，请稍后重试");
        }
    }

    @Override
    public boolean nonLock(String source,int retries){
        boolean notLock = false;
        try {
            initCurrentLock(source);
            while (retries>0){
                if (lockResource()){
                    return true;
                }
                retries--;
            }
            if (retries==0){
                notLock = true;
            }
        } catch (LockException e) {
            e.printStackTrace();
            log.error("上锁失败: {} ",e.getMessage());
            notLock = true;
            return false;
        }finally {
            //如果出现异常了，那么一定要删除
            if (notLock){
                removeResource();
            }
        }
        return !notLock;
    }

    @Override
    public void asynchronousLock(String source, Runnable runnable) {

    }

    @Override
    public boolean nonLock(String source,long timeout) {

//        try {
//            initCurrentLock(source);
//            //如果获得了，那么不要继续了
//            if (lockResource()){
//                return true;
//            }
//            //阻塞 监听
//            nextLock(timeout);
//        } catch (Exception e) {
//            log.error("error: {}", e.getMessage());
//            return false;
//        }
        return true;
    }

    @Override
    public boolean unLock() {
        if (getCount()>1){
            deCount();
            return true;
        }

        return removeResource();
    }

    private boolean removeResource(){
        try {
            String lockPath = getLockPath();
            THREAD_LOCAL.remove();
            if (!StringUtils.isEmpty(lockPath) && curatorFramework.checkExists().forPath(lockPath)!=null){
                curatorFramework.delete().forPath(lockPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //todo: 如果删除锁失败了，那么要记录日志，同时报警，进行人工干预
            return false;
        }
        return true;
    }

    private boolean lockResource() throws LockException {

        //判断是否重入了
        if (isLock()){
            incCount();
            return true;
        }

        String lockPath  = getLockPath();
        String basePath =  getBasePath();
        int currentNumber = CommonUtil.getNumber(lockPath);
        List<String> childrenPath;
        try {
            childrenPath = curatorFramework.getChildren().forPath(basePath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LockException("获取锁列表失败");
        }
        // 获取所有节点的最小节点数字
        int minNumber = CommonUtil.getMin(childrenPath);
        //如果相等，那么它就是最小的，获得锁
        if (currentNumber == minNumber){
            System.out.println("lock thread: " + Thread.currentThread().getId() +" , lock number: " + currentNumber);
            setLock();
            incCount();
            return true;
        }
        return false;
    }

    @Data
    public static class LockInfo{
        private String source;
        private String lockPath;
        private int count;
        private boolean lock = false;
//        private boolean notify = false;
    }
}
