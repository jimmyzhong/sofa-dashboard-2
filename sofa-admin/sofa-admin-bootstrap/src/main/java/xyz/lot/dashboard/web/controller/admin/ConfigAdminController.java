package xyz.lot.dashboard.web.controller.admin;

import xyz.lot.common.annotation.AjaxWrapper;
import xyz.lot.db.mongo.util.PageRequestUtil;
import xyz.lot.common.domain.PageModel;
import xyz.lot.dashboard.common.annotation.Log;
import xyz.lot.dashboard.common.constants.BusinessType;
import xyz.lot.dashboard.manage.entity.SysConfig;
import xyz.lot.common.exception.BusinessException;
import xyz.lot.dashboard.manage.security.config.PermissionConstants;
import xyz.lot.dashboard.manage.service.SysConfigService;
import xyz.lot.dashboard.common.util.ExcelUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/system/config")
public class ConfigAdminController {
    private String prefix = "system/config";

    @Autowired
    private SysConfigService sysConfigService;

    @RequiresPermissions(PermissionConstants.Config.VIEW)
    @GetMapping()
    public String config() {
        return prefix + "/config";
    }


    @RequiresPermissions(PermissionConstants.Config.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel list(SysConfig sysConfig, HttpServletRequest request) {
        sysConfig.setIsDelete(false);
        return sysConfigService.selectPage(PageRequestUtil.fromRequest(request), sysConfig);
    }

    @Log(title = "参数管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions(PermissionConstants.Config.EXPORT)
    @PostMapping("/export")
    @AjaxWrapper
    public String export(SysConfig sysConfig, HttpServletRequest request) {
        PageModel<SysConfig> list = sysConfigService.selectPage(PageRequestUtil.fromRequestIgnorePageSize(request), sysConfig);
        ExcelUtil<SysConfig> util = new ExcelUtil<SysConfig>(SysConfig.class);
        return util.exportExcel(list.getRows(), "参数数据");
    }

    /**
     * 新增参数配置
     */
    @RequiresPermissions(PermissionConstants.Config.ADD)
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存参数配置
     */
    @RequiresPermissions(PermissionConstants.Config.ADD)
    @Log(title = "参数管理", businessType = BusinessType.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public SysConfig addSave(SysConfig sysConfig) {
        if (!sysConfigService.checkConfigKeyUnique(sysConfig)) {
            throw BusinessException.build("新增参数'" + sysConfig.getConfigName() + "'失败，参数键名已存在");
        }
        return sysConfigService.insert(sysConfig);
    }

    /**
     * 修改参数配置
     */
    @RequiresPermissions(PermissionConstants.Config.EDIT)
    @GetMapping("/edit/{configId}")
    public String edit(@PathVariable("configId") Long configId, ModelMap mmap) {
        mmap.put("config", sysConfigService.selectByPId(configId));
        return prefix + "/edit";
    }

    @RequiresPermissions(PermissionConstants.Config.EDIT)
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @AjaxWrapper
    public SysConfig editSave(SysConfig sysConfig) {
        if (!sysConfigService.checkConfigKeyUnique(sysConfig)) {
            throw BusinessException.build("修改参数'" + sysConfig.getConfigName() + "'失败，参数键名已存在");
        }
        return sysConfigService.update(sysConfig);
    }


    @RequiresPermissions(PermissionConstants.Config.REMOVE)
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(String ids) {
        return sysConfigService.deleteByPIds(ids);
    }


    @PostMapping("/checkConfigKeyUnique")
    @ResponseBody
    public boolean checkConfigKeyUnique(SysConfig sysConfig) {
        return sysConfigService.checkConfigKeyUnique(sysConfig);
    }
}