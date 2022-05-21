package com.jufeng.distributed.box.mysql.lock;

import com.jufeng.distributed.box.mysql.lock.support.UniqueLockSupport;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-21 18:03
 **/
@SpringBootTest
public class UniqueLockSupportTest {

    @Autowired
    private UniqueLockSupport uniqueLockSupport;


    /**
     * 库存
     */
    private static int storeCount = 100;

    @Test
    public void testLock(){

        //这里模拟需要对库存进行扣减
       //这里用两个线程，来模拟多个进行对一个资源的操作
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(new Task(uniqueLockSupport));
        executorService.submit(new Task(uniqueLockSupport));

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int decStoreCount(){
        return   storeCount--;
    }

    public static class Task implements Runnable{

        private UniqueLockSupport uniqueLockSupport;

        public Task(UniqueLockSupport uniqueLockSupport) {
            this.uniqueLockSupport = uniqueLockSupport;
        }

        @Override
        public void run() {
            try {
                uniqueLockSupport.lock("decStoreCount");
                uniqueLockSupport.lock("decStoreCount");
                decStoreCount();
            }finally {
                uniqueLockSupport.unLock("decStoreCount");
                uniqueLockSupport.unLock("decStoreCount");
            }


        }
    }
}
