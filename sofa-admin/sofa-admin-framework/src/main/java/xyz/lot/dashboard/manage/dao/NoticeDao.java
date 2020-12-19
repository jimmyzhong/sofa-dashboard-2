package xyz.lot.dashboard.manage.dao;

import xyz.lot.dashboard.manage.entity.SysNotice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeDao extends MongoRepository<SysNotice, Long> {


    SysNotice findByNoticeId(Long configId);

    int deleteAllByNoticeIdIn(List<Long> delIds);
}
