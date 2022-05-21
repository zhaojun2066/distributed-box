package com.jufeng.distributed.box.mysql.lock.repository;

import com.jufeng.distributed.box.mysql.lock.entity.UniqueLock;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-04-26 23:37
 **/
public interface UniqueLockRepository extends JpaRepository<UniqueLock, Integer> {
}
