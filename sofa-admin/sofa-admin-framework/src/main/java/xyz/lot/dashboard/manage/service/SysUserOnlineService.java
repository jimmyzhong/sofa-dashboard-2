package xyz.lot.dashboard.manage.service;

import xyz.lot.common.domain.PageModel;
import xyz.lot.common.domain.PageRequest;
import xyz.lot.dashboard.manage.entity.SysUserOnline;

import java.util.Date;
import java.util.List;

public interface SysUserOnlineService {

    public void batchDeleteOnline(List<String> sessionIds);

    public void saveOnline(SysUserOnline online);

    public void forceLogout(String sessionId);

    public List<SysUserOnline> selectOnlineByLastAccessTime(Date lastAccessTime);

    SysUserOnline selectByPId(String sessionId);

    PageModel<SysUserOnline> selectPage(PageRequest fromRequest, SysUserOnline sysUserOnline);

    void deleteById(String sessionId);

    List<SysUserOnline> findAll();
}
