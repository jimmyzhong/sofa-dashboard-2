package xyz.lot.dashboard.manage.dao;

import xyz.lot.dashboard.manage.entity.SysLoginInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginInfoDao extends MongoRepository<SysLoginInfo, Long> {

    SysLoginInfo findByInfoId(Long infoId);

    int deleteByInfoIdIn(List<Long> infoIds);
}
