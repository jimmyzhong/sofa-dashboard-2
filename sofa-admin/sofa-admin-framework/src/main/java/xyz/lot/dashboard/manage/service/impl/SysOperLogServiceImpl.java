package xyz.lot.dashboard.manage.service.impl;

import xyz.lot.db.mongo.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import xyz.lot.dashboard.manage.entity.SysOperLog;
import xyz.lot.dashboard.manage.service.SysOperLogService;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SysOperLogServiceImpl extends CrudBaseServiceImpl<Long, SysOperLog> implements SysOperLogService {

}
