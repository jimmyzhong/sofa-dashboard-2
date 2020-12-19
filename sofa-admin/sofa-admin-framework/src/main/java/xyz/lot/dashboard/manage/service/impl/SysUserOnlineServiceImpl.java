package xyz.lot.dashboard.manage.service.impl;

import xyz.lot.common.domain.PageModel;
import xyz.lot.common.domain.PageRequest;
import lombok.extern.slf4j.Slf4j;
import xyz.lot.dashboard.manage.dao.UserOnlineDao;
import xyz.lot.dashboard.manage.entity.SysUserOnline;
import xyz.lot.common.exception.BusinessException;
import xyz.lot.dashboard.manage.service.SysUserOnlineService;
import xyz.lot.db.mongo.util.PageRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SysUserOnlineServiceImpl implements SysUserOnlineService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserOnlineDao userOnlineDao;

    @Override
    public void batchDeleteOnline(List<String> sessions) {
        for (String sessionId : sessions) {
            SysUserOnline sysUserOnline = userOnlineDao.findBySessionId(sessionId);
            if (sysUserOnline != null) {
                userOnlineDao.delete(sysUserOnline);
            }
        }
    }

    //@Transactional
    @Override
    public void saveOnline(SysUserOnline online) {
        if (StringUtils.isBlank(online.getSessionId())) {
            throw BusinessException.build("session id is null");
        }
        userOnlineDao.save(online);
    }

    //@Transactional
    @Override
    public void forceLogout(String sessionId) {
        userOnlineDao.deleteById(sessionId);
    }

    @Override
    public List<SysUserOnline> selectOnlineByLastAccessTime(Date lastAccessTime) {
        Assert.notNull(lastAccessTime,"");
        Query query = new Query();
        query.addCriteria(Criteria.where("lastAccessTime").lte(lastAccessTime));
        List<SysUserOnline> users = mongoTemplate.find(query, SysUserOnline.class);
        return users;
    }

    @Override
    public SysUserOnline selectByPId(String sessionId) {
        return userOnlineDao.findBySessionId(sessionId);
    }

    @Override
    public PageModel<SysUserOnline> selectPage(PageRequest fromRequest, SysUserOnline sysUserOnline) {
        Query query = new Query();
        query.addCriteria(Criteria.where("loginName").exists(true));
        List<SysUserOnline> results = selectList(query,fromRequest,sysUserOnline);
        long count = doCount(query);
        return PageModel.instance(count, results);
    }

    @Override
    public void deleteById(String sessionId) {
        userOnlineDao.deleteById(sessionId);
    }

    @Override
    public List<SysUserOnline> findAll() {
        return userOnlineDao.findAll();
    }

    public List<SysUserOnline> selectList(Query query, PageRequest request, SysUserOnline target) {
        if (query == null) {
            query = new Query();
        }
        if(request != null)
            PageRequestUtil.injectQuery(request,query);

        List<SysUserOnline> results = mongoTemplate.find(query, SysUserOnline.class);
        return results;
    }

    private long doCount(Query query){
        if(query == null)
            query = new Query();
        return mongoTemplate.count(query, SysUserOnline.class);
    }
}
