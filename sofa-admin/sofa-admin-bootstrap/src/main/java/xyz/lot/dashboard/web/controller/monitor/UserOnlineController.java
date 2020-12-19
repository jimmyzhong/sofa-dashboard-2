package xyz.lot.dashboard.web.controller.monitor;

import xyz.lot.common.annotation.AjaxWrapper;
import xyz.lot.dashboard.manage.security.service.SysShiroService;
import xyz.lot.db.mongo.util.PageRequestUtil;
import xyz.lot.common.domain.PageModel;
import xyz.lot.dashboard.common.annotation.Log;
import xyz.lot.dashboard.common.constants.BusinessType;
import xyz.lot.dashboard.manage.entity.SysUserOnline;
import xyz.lot.dashboard.manage.security.config.PermissionConstants;
import xyz.lot.dashboard.manage.security.session.OnlineSession;
import xyz.lot.dashboard.manage.service.SysUserOnlineService;
import xyz.lot.dashboard.manage.security.UserInfoContextHelper;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/monitor/online")
public class UserOnlineController {

    private String prefix = "monitor/online";

    @Autowired
    private SysUserOnlineService sysUserOnlineService;

    @Autowired
    private SysShiroService sysShiroService;

    @RequiresPermissions(PermissionConstants.UserOnline.VIEW)
    @GetMapping
    public String online() {
        return prefix + "/online";
    }

    @RequiresPermissions(PermissionConstants.UserOnline.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<SysUserOnline> list(HttpServletRequest request, SysUserOnline sysUserOnline) {
        return sysUserOnlineService.selectPage(PageRequestUtil.fromRequest(request), sysUserOnline);
    }

    @RequiresPermissions(PermissionConstants.UserOnline.FORCE_LOGOUT)
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @PostMapping("/batchForceLogout")
    @AjaxWrapper
    public boolean batchForceLogout(@RequestParam("ids[]") String[] ids) {
        for (String sessionId : ids) {
            forceLogout(sessionId);
        }
        return true;
    }

    @RequiresPermissions(PermissionConstants.UserOnline.FORCE_LOGOUT)
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @PostMapping("/forceLogout")
    @AjaxWrapper
    public boolean forceLogout(String sessionId) {
        if (sessionId.equals(UserInfoContextHelper.getSessionId())) {
            //throw BusinessException.build("当前登录用户无法强退");
        }

        SysUserOnline sysUserOnline = sysUserOnlineService.selectByPId(sessionId);
        if(sysUserOnline !=null){
            sysUserOnline.setStatus(SysUserOnline.OnlineStatus.off_line);
            sysUserOnlineService.saveOnline(sysUserOnline);
        }

        OnlineSession session = (OnlineSession) sysShiroService.getSession(sessionId);
        if(session != null) {
            session.setStatus(SysUserOnline.OnlineStatus.off_line);
            sysShiroService.saveSession(session);
        }

//        if (session == null) {
//            throw BusinessException.build("用户已下线");
//        }

        return true;
    }
}
