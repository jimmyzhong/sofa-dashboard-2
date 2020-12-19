package xyz.lot.dashboard.manage.dao;

import xyz.lot.dashboard.manage.entity.SysUserOnline;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOnlineDao extends MongoRepository<SysUserOnline, String> {

    SysUserOnline findBySessionId(String sessionId);

}
