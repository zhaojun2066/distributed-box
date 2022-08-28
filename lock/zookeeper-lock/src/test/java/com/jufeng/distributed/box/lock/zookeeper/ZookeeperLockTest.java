package com.jufeng.distributed.box.lock.zookeeper;

import com.jufeng.distributed.box.lock.zookeeper.common.CommonUtil;
import com.jufeng.distributed.box.lock.zookeeper.common.LockException;
import com.jufeng.distributed.box.lock.zookeeper.common.LookupSingleton;
import com.jufeng.distributed.box.lock.zookeeper.support.ZookeeperLock;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-24 22:17
 **/
@SpringBootTest
public class ZookeeperLockTest {

//    @Autowired
//    private ZookeeperLock zookeeperLock;
    @Autowired
    private LookupSingleton lookupSingleton;
    @Autowired
    private CuratorFramework curatorFramework;

    private static int storeCount = 100;




    @Test
    public void zkTest(){
        try {
            String source = "resource0";
            String base = "/"+source;
            // 创建临时节点,这里肯定谁最小是谁先创建出来
            final String s = curatorFramework.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(base + "/lock_");
            System.out.println("lock path: "  + s);
            System.out.println("number: "  + CommonUtil.getNumber(s));
            final String s1 = curatorFramework.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(base + "/lock_");
            System.out.println("lock path: "  + s1);
            System.out.println("number: "  + CommonUtil.getNumber(s1));
            final List<String> strings = curatorFramework.getChildren().forPath(base);
            strings.forEach(x-> System.out.println("children=="+x));
            final int min = CommonUtil.getMin(strings);
            System.out.println("min===" + min);
            final String brother = CommonUtil.getYoungerBrother(source,strings, s1);
            System.out.println("bro====" +brother);


            final NodeCache cache = new NodeCache(curatorFramework,brother,false);
            cache.start(true);
            cache.getListenable().addListener(() -> {
                ChildData cdata=cache.getCurrentData();
                if(null==cdata) {
                    System.out.println("节点发生了变化，可能刚刚被删除！");
                    cache.close();//关闭监听
                    //lock(source);
                }
            });

            curatorFramework.delete().forPath("/aa");
            System.out.println("=====================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void lockTest(){
//这里模拟需要对库存进行扣减
        //这里用两个线程，来模拟多个进行对一个资源的操作
        ExecutorService executorService = Executors.newFixedThreadPool(12);
        executorService.submit(new Task(lookupSingleton.zookeeperLock()));
        executorService.submit(new Task(lookupSingleton.zookeeperLock()));
        executorService.submit(new Task(lookupSingleton.zookeeperLock()));
        executorService.submit(new Task(lookupSingleton.zookeeperLock()));
        executorService.submit(new Task(lookupSingleton.zookeeperLock()));
        executorService.submit(new Task(lookupSingleton.zookeeperLock()));
        executorService.submit(new Task(lookupSingleton.zookeeperLock()));

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static int decStoreCount(){
        return   --storeCount;
    }

    public static class Task implements Runnable{

        private ZookeeperLock zookeeperLock;

        public Task(ZookeeperLock uniqueLockSupport) {
            this.zookeeperLock = uniqueLockSupport;
        }

        @Override
        public void run() {
            try {
//                zookeeperLock.lock("decStoreCount");
                zookeeperLock.lock("decStoreCount");
                final int i = decStoreCount();
                System.out.println("=============thread: " + Thread.currentThread().getId()+" =>i : " + i);
            } catch (LockException e) {
                e.printStackTrace();
            } finally {
//                zookeeperLock.unLock();
                zookeeperLock.unLock();
            }

//            try {
//                非阻塞
//                final boolean status = zookeeperLock.nonLock("decStoreCount", 500L);
////                final boolean status = zookeeperLock.nonLock("decStoreCount", 10);
//                if (status){
//                    final int i = decStoreCount();
//                    System.out.println("=============thread: " + Thread.currentThread().getId()+" =>i : " + i);
//                }
//            }  finally {
//                zookeeperLock.unLock();
//            }


        }
    }
}
