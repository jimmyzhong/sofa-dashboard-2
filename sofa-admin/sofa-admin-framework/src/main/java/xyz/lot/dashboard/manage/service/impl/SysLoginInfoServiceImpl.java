package xyz.lot.dashboard.manage.service.impl;

import xyz.lot.db.mongo.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import xyz.lot.dashboard.manage.entity.SysLoginInfo;
import xyz.lot.dashboard.manage.service.SysLoginInfoService;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SysLoginInfoServiceImpl extends CrudBaseServiceImpl<Long,SysLoginInfo> implements SysLoginInfoService {

}
