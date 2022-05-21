package com.jufeng.distributed.box.mysql.lock.service.impl;

import com.jufeng.distributed.box.mysql.lock.entity.UniqueLock;
import com.jufeng.distributed.box.mysql.lock.repository.UniqueLockRepository;
import com.jufeng.distributed.box.mysql.lock.service.UniqueLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: distributed-box
 * @description:
 * @author: JuFeng(ZhaoJun)
 * @create: 2022-05-21 16:35
 **/
@Service
public class UniqueLockServiceImpl implements UniqueLockService {

    @Autowired
    private UniqueLockRepository uniqueLockRepository;


    @Transactional
    @Override
    public boolean insert(UniqueLock uniqueLock) {
        final UniqueLock save = uniqueLockRepository.save(uniqueLock);
        return  save!=null;
    }

    @Override
    public UniqueLock findByResource(String resource) {
        UniqueLock lock = new UniqueLock();
        lock.setLockResource(resource);
        return uniqueLockRepository.findOne(Example.of(lock)).orElse(null);
    }

    @Override
    public boolean incCount(Integer id) {
        final UniqueLock lock = uniqueLockRepository.findById(id).get();
        lock.setLockCount(lock.getLockCount()+1);
        final UniqueLock save = uniqueLockRepository.save(lock);
        return save!=null;
    }

    @Override
    public boolean deCount(Integer id) {
        final UniqueLock lock = uniqueLockRepository.findById(id).get();
        lock.setLockCount(lock.getLockCount()-1);
        final UniqueLock save = uniqueLockRepository.save(lock);
        return save!=null;
    }

    @Override
    public long count(String resource) {
        UniqueLock lock = new UniqueLock();
        lock.setLockResource(resource);
        return uniqueLockRepository.count(Example.of(lock));
    }

    @Override
    public boolean deleteById(Integer id) {
        uniqueLockRepository.deleteById(id);
        return true;
    }
}
