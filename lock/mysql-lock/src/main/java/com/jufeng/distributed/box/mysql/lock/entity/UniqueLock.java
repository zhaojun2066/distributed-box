package com.jufeng.distributed.box.mysql.lock.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-04-26 23:32
 **/

@Entity
@Table(name = "unique_lock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniqueLock {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY) //设置主键自增
    private Integer id;

    /**
     * 互斥资源标识
     */
    private String lockResource;
    /**
     * 重入的次数
     */
    private Integer lockCount;

    /**
     * 锁描述
     */
    private String lockDesc;


    private String lockOwner;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
