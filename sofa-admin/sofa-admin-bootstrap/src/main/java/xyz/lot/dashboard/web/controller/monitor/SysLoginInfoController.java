package xyz.lot.dashboard.web.controller.monitor;

import xyz.lot.dashboard.manage.security.service.PasswordService;
import xyz.lot.common.annotation.AjaxWrapper;
import xyz.lot.db.mongo.util.PageRequestUtil;
import xyz.lot.common.domain.PageModel;
import xyz.lot.dashboard.common.annotation.Log;
import xyz.lot.dashboard.common.constants.BusinessType;
import xyz.lot.dashboard.manage.entity.SysLoginInfo;
import xyz.lot.dashboard.manage.security.config.PermissionConstants;
import xyz.lot.dashboard.manage.service.SysLoginInfoService;
import xyz.lot.dashboard.common.util.ExcelUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/monitor/logininfor")
public class SysLoginInfoController {
    private String prefix = "monitor/logininfor";

    @Autowired
    private SysLoginInfoService sysLoginInfoService;

    @Autowired
    private PasswordService passwordService;

    @RequiresPermissions(PermissionConstants.LoginInfo.VIEW)
    @GetMapping()
    public String logininfor() {
        return prefix + "/logininfor";
    }

    @RequiresPermissions(PermissionConstants.LoginInfo.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<SysLoginInfo> list(HttpServletRequest request, SysLoginInfo sysLoginInfo) {
        return sysLoginInfoService.selectPage(PageRequestUtil.fromRequest(request), sysLoginInfo);
    }

    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @RequiresPermissions(PermissionConstants.LoginInfo.EXPORT)
    @PostMapping("/export")
    @AjaxWrapper
    public String export(HttpServletRequest request, SysLoginInfo sysLoginInfo) {
        List<SysLoginInfo> list = sysLoginInfoService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysLoginInfo);
        ExcelUtil<SysLoginInfo> util = new ExcelUtil<SysLoginInfo>(SysLoginInfo.class);
        return util.exportExcel(list, "登录日志");
    }

    @RequiresPermissions(PermissionConstants.LoginInfo.REMOVE)
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(String ids) {
        return sysLoginInfoService.removeByPIds(ids);
    }

    @RequiresPermissions(PermissionConstants.LoginInfo.REMOVE)
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @PostMapping("/clean")
    @AjaxWrapper
    public long clean() {
        return sysLoginInfoService.clearAll();
    }

}
