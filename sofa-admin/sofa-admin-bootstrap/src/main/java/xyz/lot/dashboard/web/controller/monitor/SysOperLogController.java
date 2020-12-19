package xyz.lot.dashboard.web.controller.monitor;

import xyz.lot.common.annotation.AjaxWrapper;
import xyz.lot.db.mongo.util.PageRequestUtil;
import xyz.lot.common.domain.PageModel;
import xyz.lot.dashboard.common.annotation.Log;
import xyz.lot.dashboard.common.constants.BusinessType;
import xyz.lot.dashboard.manage.entity.SysOperLog;
import xyz.lot.dashboard.manage.security.config.PermissionConstants;
import xyz.lot.dashboard.manage.service.SysOperLogService;
import xyz.lot.dashboard.common.util.ExcelUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/monitor/operlog")
public class SysOperLogController {

    private String prefix = "monitor/operlog";

    @Autowired
    private SysOperLogService sysOperLogService;

    @RequiresPermissions(PermissionConstants.OperLog.VIEW)
    @GetMapping()
    public String operlog() {
        return prefix + "/operlog";
    }

    @RequiresPermissions(PermissionConstants.OperLog.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel list(HttpServletRequest request, SysOperLog sysOperLog) {
        return sysOperLogService.selectPage(PageRequestUtil.fromRequest(request), sysOperLog);
    }

    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @RequiresPermissions(PermissionConstants.OperLog.EXPORT)
    @PostMapping("/export")
    @AjaxWrapper
    public String export(HttpServletRequest request, SysOperLog sysOperLog) {
        List<SysOperLog> list = sysOperLogService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysOperLog);
        ExcelUtil<SysOperLog> util = new ExcelUtil<SysOperLog>(SysOperLog.class);
        return util.exportExcel(list, "操作日志");
    }

    @RequiresPermissions(PermissionConstants.OperLog.REMOVE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(String ids) {
        return sysOperLogService.removeByPIds(ids);
    }

    @RequiresPermissions(PermissionConstants.OperLog.VIEW)
    @GetMapping("/detail/{operId}")
    public String detail(@PathVariable("operId") Long operId, ModelMap mmap) {
        mmap.put("operLog", sysOperLogService.selectByPId(operId));
        return prefix + "/detail";
    }

    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @RequiresPermissions(PermissionConstants.OperLog.REMOVE)
    @PostMapping("/clean")
    @AjaxWrapper
    public void clean() {
        sysOperLogService.clearAll();
    }

}
