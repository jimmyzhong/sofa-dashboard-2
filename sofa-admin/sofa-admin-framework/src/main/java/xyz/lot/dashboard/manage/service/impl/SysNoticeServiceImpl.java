package xyz.lot.dashboard.manage.service.impl;

import xyz.lot.db.mongo.service.CrudBaseServiceImpl;
import xyz.lot.dashboard.manage.entity.SysNotice;
import xyz.lot.dashboard.manage.service.SysNoticeService;
import org.springframework.stereotype.Service;

@Service
public class SysNoticeServiceImpl extends CrudBaseServiceImpl<Long,SysNotice> implements SysNoticeService {

}
