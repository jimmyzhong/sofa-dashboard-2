package xyz.lot.dashboard.manage.dao;

import xyz.lot.dashboard.manage.entity.SysOperLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperLogDao extends MongoRepository<SysOperLog, Long> {

    SysOperLog findByOperId(Long operId);

    int deleteByOperIdIn(List<Long> delIds);
}
