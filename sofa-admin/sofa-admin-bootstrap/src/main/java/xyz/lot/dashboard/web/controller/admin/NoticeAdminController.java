package xyz.lot.dashboard.web.controller.admin;

import xyz.lot.common.annotation.AjaxWrapper;
import xyz.lot.db.mongo.util.PageRequestUtil;
import xyz.lot.common.domain.PageModel;
import xyz.lot.dashboard.common.annotation.Log;
import xyz.lot.dashboard.common.constants.BusinessType;
import xyz.lot.dashboard.manage.entity.SysNotice;
import xyz.lot.dashboard.manage.security.UserInfoContextHelper;
import xyz.lot.dashboard.manage.security.config.PermissionConstants;
import xyz.lot.dashboard.manage.service.SysNoticeService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/system/notice")
public class NoticeAdminController {
    private String prefix = "system/notice";

    @Autowired
    private SysNoticeService sysNoticeService;

    @RequiresPermissions(PermissionConstants.Notice.VIEW)
    @GetMapping()
    public String notice() {
        return prefix + "/notice";
    }

    @RequiresPermissions(PermissionConstants.Notice.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel list(SysNotice sysNotice, HttpServletRequest request) {
        sysNotice.setIsDelete(false);
        return sysNoticeService.selectPage(PageRequestUtil.fromRequest(request), sysNotice);
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }


    @RequiresPermissions(PermissionConstants.Notice.ADD)
    @Log(title = "通知公告", businessType = BusinessType.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public SysNotice addSave(@Validated SysNotice sysNotice) {
        sysNotice.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        sysNotice.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
        return sysNoticeService.insert(sysNotice);
    }

    @RequiresPermissions(PermissionConstants.Notice.EDIT)
    @GetMapping("/edit/{noticeId}")
    public String edit(@PathVariable("noticeId") Long noticeId, ModelMap mmap) {
        mmap.put("notice", sysNoticeService.selectByPId(noticeId));
        return prefix + "/edit";
    }

    @RequiresPermissions(PermissionConstants.Notice.EDIT)
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @AjaxWrapper
    public SysNotice editSave(@Validated SysNotice sysNotice) {
        sysNotice.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
        return sysNoticeService.update(sysNotice);
    }

    @RequiresPermissions(PermissionConstants.Notice.REMOVE)
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(String ids) {
        return sysNoticeService.deleteByPIds(ids);
    }
}