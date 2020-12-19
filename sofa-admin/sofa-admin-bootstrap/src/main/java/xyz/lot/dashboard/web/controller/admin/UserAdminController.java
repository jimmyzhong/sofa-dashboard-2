package xyz.lot.dashboard.web.controller.admin;

import xyz.lot.dashboard.common.expection.user.UserHasNotPermissionException;
import xyz.lot.dashboard.manage.entity.SysRole;
import xyz.lot.dashboard.manage.service.*;
import xyz.lot.db.mongo.util.PageRequestUtil;
import xyz.lot.common.domain.PageModel;
import xyz.lot.common.domain.PageRequest;
import xyz.lot.common.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import xyz.lot.common.annotation.AjaxWrapper;
import xyz.lot.dashboard.common.annotation.Log;
import xyz.lot.dashboard.common.constants.BusinessType;
import xyz.lot.dashboard.common.constants.Global;
import xyz.lot.dashboard.manage.entity.SysDept;
import xyz.lot.dashboard.manage.entity.SysUser;
import xyz.lot.common.exception.BusinessException;
import xyz.lot.dashboard.manage.security.config.PermissionConstants;
import xyz.lot.dashboard.manage.security.service.PasswordService;
import xyz.lot.common.util.Convert;
import xyz.lot.dashboard.common.util.ExcelUtil;
import xyz.lot.dashboard.manage.security.UserInfoContextHelper;
import xyz.lot.dashboard.common.util.UserConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.lot.dashboard.manage.service.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/system/user")
public class UserAdminController {

    private String prefix = "system/user";

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysPostService sysPostService;

    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private SysConfigService sysConfigService;

    @RequiresPermissions(PermissionConstants.User.VIEW)
    @RequestMapping
    public String user() {
        return prefix + "/user";
    }

    @RequiresPermissions(PermissionConstants.User.VIEW)
    //@PreAuthorize("hasAnyAuthority('"+ PermissionConstants.User.VIEW + "')")
    @RequestMapping("list")
    @AjaxWrapper
    public PageModel<UserInfo> list(@RequestParam(value = "loginName", required = false) String loginName,
                                    @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                                    @RequestParam(value = "deptId", required = false) Long deptId,
                                    @RequestParam(value = "status", required = false) String status,
                                    HttpServletRequest request) {
        SysUser searchUser = new SysUser();
        searchUser.setPhoneNumber(phoneNumber);
        searchUser.setLoginName(loginName);
        searchUser.setDeptId(deptId);
        searchUser.setIsDelete(false);
        if(StringUtils.isNotBlank(status)) {
            searchUser.setStatus(status);
        }

        PageRequest pageRequest = PageRequestUtil.fromRequest(request);
        if(!UserInfoContextHelper.getLoginUser().isHasAllDeptPerm())
            pageRequest.setDepts(UserInfoContextHelper.getLoginUser().getScopeData(PermissionConstants.User.VIEW));

        PageModel<SysUser> pe = sysUserService.getPage(pageRequest, searchUser);
        List<SysUser> lue = pe.getRows();
        if(pe == null)
            return null;
        List<UserInfo> uinfs = lue.stream().map(e -> UserConvertUtil.convert(e)).collect(Collectors.toList());
        return PageModel.instance(pe.getCount(),uinfs);
    }

    @RequiresPermissions(PermissionConstants.User.ADD)
    @GetMapping("/add")
    public String add(ModelMap mmap) {
        //只查询自己有权限的角色
        List<SysRole> useRoles = sysRoleService.selectAllVisibleRolesByUserId(UserInfoContextHelper.getCurrentUserId(), null);
        mmap.put("roles", useRoles);
        mmap.put("posts", sysPostService.selectAll());
        return prefix + "/add";
    }

    @RequiresPermissions(PermissionConstants.User.ADD)
    @Log(title = "用户管理", businessType = BusinessType.ADD)
    @PostMapping("add")
    @AjaxWrapper
    public UserInfo addUser(@Validated SysUser user) {

        UserInfoContextHelper.getLoginUser().checkScopePermission(PermissionConstants.User.ADD,user.getDeptId());

        SysUser dbUser = new SysUser();
        if (StringUtils.isBlank(user.getLoginName())) {
            throw BusinessException.build("登录名不能为空");
        }
        if (StringUtils.isBlank(user.getLoginName())) {
            throw BusinessException.build("登录名不能为空");
        }
        if (!sysUserService.checkLoginNameUnique(user)) {
            throw BusinessException.build("用登录名字已经存在");
        }
        if (!sysUserService.checkPhoneUnique(user)) {
            throw BusinessException.build("手机号已经存在");
        }
        if (StringUtils.isBlank(user.getPassword())) {
            throw BusinessException.build("密码不能为空");
        } else {
            dbUser.setSalt(Global.getSalt());
            dbUser.setPassword(passwordService.encryptPassword(user.getPassword(), Global.getSalt()));
            dbUser.setPasswordUpdateTime(new Date());
        }

        dbUser.setStatus(user.getStatus());
        dbUser.setIsDelete(false);
        dbUser.setPhoneNumber(user.getPhoneNumber());
        dbUser.setEmail(user.getEmail());
        dbUser.setUserName(user.getUserName());
        dbUser.setLoginName(user.getLoginName());
        dbUser.setSex(user.getSex());
        dbUser.setPostIds(user.getPostIds());
        if (user.getDeptId() != null) {
            SysDept sysDept = sysDeptService.selectDeptByDeptId(user.getDeptId());
            dbUser.setDeptId(user.getDeptId());
            dbUser.setDeptName(sysDept.getDeptName());
        }
        dbUser.setRoleIds(user.getRoleIds());
        dbUser.setRemark(user.getRemark());

        dbUser.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        dbUser.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        return UserConvertUtil.convert(sysUserService.saveUserAndPerms(dbUser));
    }

    @RequiresPermissions(PermissionConstants.User.EDIT)
    @GetMapping("/edit/{userId}")
    public String edit(@PathVariable("userId") Long userId, ModelMap mmap) {
        SysUser sysUser = sysUserService.findUser(userId);
        mmap.put("user", sysUser);

        if(UserInfoContextHelper.getLoginUser().hashScopePermission(PermissionConstants.User.ROLE,sysUser.getDeptId())) {
            mmap.put("showVisibleScppe", true);
            //只查询有权限的角色 check
            List<SysRole> useRoles = sysRoleService.selectAllVisibleRolesByUserId(UserInfoContextHelper.getCurrentUserId(),userId);
            mmap.put("roles", useRoles);
        } else {
            mmap.put("showVisibleScppe", false);
        }

        mmap.put("posts", sysPostService.selectPostsByUserId(userId));
        return prefix + "/edit";
    }

    @RequiresPermissions(PermissionConstants.User.EDIT)
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("edit")
    @AjaxWrapper
    public UserInfo editUser(SysUser user) {
        UserInfoContextHelper.checkScopePermission(PermissionConstants.User.EDIT,user.getDeptId());
        SysUser dbUser;
        if (user == null) {
            throw BusinessException.build("user不能为空");
        }
        if (user.getUserId() == null) {
            throw BusinessException.build("用户ID不能为空");
        }
        //修改
        dbUser = sysUserService.findUser(user.getUserId());

        if(dbUser.getDeptId() != null)
            UserInfoContextHelper.checkScopePermission(PermissionConstants.User.EDIT,dbUser.getDeptId());

        if (StringUtils.isBlank(user.getLoginName())) {
            throw BusinessException.build("loginName不能为空");
        }
        if (!sysUserService.checkLoginNameUnique(user)) {
            throw BusinessException.build("用户名字已经存在");
        }
        if (!sysUserService.checkPhoneUnique(user)) {
            throw BusinessException.build("手机号已经存在");
        }
        dbUser.setStatus(user.getStatus());
        dbUser.setAvatar(user.getAvatar());
        dbUser.setPhoneNumber(user.getPhoneNumber());
        dbUser.setEmail(user.getEmail());
        dbUser.setUserName(user.getUserName());
        dbUser.setSex(user.getSex());
        dbUser.setPostIds(user.getPostIds());
        if (user.getDeptId() != null) {
            dbUser.setDeptId(user.getDeptId());
            SysDept sysDept = sysDeptService.selectDeptByDeptId(user.getDeptId());
            dbUser.setDeptName(sysDept.getDeptName());
        }

        if(UserInfoContextHelper.getLoginUser().hashScopePermission(PermissionConstants.User.ROLE,user.getDeptId())) {
            List<Long> toSaveRoleIds = filterRoles(user.getUserId(), user.getRoleIds() == null ? new ArrayList<>() : Arrays.asList(user.getRoleIds()));
            dbUser.setRoleIds(toSaveRoleIds.toArray(new Long[]{}));
        }

        dbUser.setRemark(user.getRemark());
        dbUser.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        sysUserService.checkUserAllowed(user,"修改");
        return UserConvertUtil.convert(sysUserService.saveUserAndPerms(dbUser));
    }

//    @RequiresPermissions(PermissionConstants.User.VIEW)
//    @RequestMapping("count")
//    public long count(@RequestParam(value = "loginName", required = false) String username,
//                      @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
//                      @RequestParam(value = "deptId", required = false) Long deptId,
//                      @RequestParam(value = "beginTime", required = false) String beginTime,
//                      @RequestParam(value = "endTime", required = false) String endTime) {
//        SysUser searchUser = new SysUser();
//        searchUser.setPhoneNumber(phoneNumber);
//        searchUser.setLoginName(username);
//        searchUser.setDeptId(deptId);
//        Date bTime = TimeUtil.parseDate_yyyyMMdd_hl(beginTime);
//        Date eTime = TimeUtil.parseDate_yyyyMMdd_hl(endTime);
//
//        return sysUserService.getListSize(searchUser, bTime, eTime);
//    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions(PermissionConstants.User.EXPORT)
    @PostMapping("/export")
    @AjaxWrapper
    public String export(HttpServletRequest request, SysUser user) {
        user.setIsDelete(false);
        PageModel<SysUser> list = sysUserService.getPage(PageRequestUtil.fromRequestIgnorePageSize(request), user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        return util.exportExcel(list.getRows(), "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @RequiresPermissions(PermissionConstants.User.IMPORT)
    @PostMapping("/importData")
    @AjaxWrapper
    public String importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<SysUser> util = new ExcelUtil<>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String operName = UserInfoContextHelper.getCurrentLoginName();
        String message = sysUserService.importUser(userList, updateSupport, operName);
        return message;
    }

    @RequiresPermissions(PermissionConstants.User.VIEW)
    @GetMapping("/importTemplate")
    @AjaxWrapper
    public String importTemplate() {
        ExcelUtil<SysUser> util = new ExcelUtil<>(SysUser.class);
        return util.importTemplateExcel("用户数据");
    }


    @RequiresPermissions(PermissionConstants.User.REMOVE)
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(String ids) {
        Long[] userIds = Convert.toLongArray(ids);
        List<SysUser> users = sysUserService.findUsersByUserIds(userIds);
        if(users != null ){
            users.forEach(e ->
                    UserInfoContextHelper.checkScopePermission(PermissionConstants.User.REMOVE,e.getDeptId())
            );
        }
        return sysUserService.deleteUserByIds(ids);
    }

    /**
     * 校验用户名
     */
    @PostMapping("/checkLoginNameUnique")
    @ResponseBody
    public boolean checkLoginNameUnique(SysUser user) {
        return sysUserService.checkLoginNameUnique(user);
    }

    /**
     * 校验手机号码
     */
    @PostMapping("/checkPhoneUnique")
    @ResponseBody
    public boolean checkPhoneUnique(SysUser user) {
        return sysUserService.checkPhoneUnique(user);
    }

    /**
     * 校验email邮箱
     */
    @PostMapping("/checkEmailUnique")
    @ResponseBody
    public boolean checkEmailUnique(SysUser user) {
        return sysUserService.checkEmailUnique(user);
    }

    @Log(title = "重置密码", businessType = BusinessType.UPDATE)
    @RequiresPermissions(PermissionConstants.User.RESET_PWD)
    @GetMapping("/resetPwd/{userId}")
    public String resetPwd(@PathVariable("userId") Long userId, ModelMap mmap) {
        mmap.put("user", sysUserService.findUser(userId));
        return prefix + "/resetPwd";
    }

    @RequiresPermissions(PermissionConstants.User.RESET_PWD)
    @Log(title = "重置密码", businessType = BusinessType.UPDATE)
    @PostMapping("/resetPwd")
    @AjaxWrapper
    public void resetPwd(SysUser user) {

        SysUser dbUser = sysUserService.findUser(user.getUserId());
        UserInfoContextHelper.checkScopePermission(PermissionConstants.User.RESET_PWD,dbUser.getDeptId());

        user.setSalt(Global.getSalt());
        user.setPassword(passwordService.encryptPassword(user.getPassword(), user.getSalt()));
        //解锁账户
        sysUserService.resetLoginFail(dbUser.getUserId());

        dbUser.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        sysUserService.checkUserAllowed(user,"重置");
        sysUserService.resetUserPwd(user.getUserId(), user.getPassword(),user.getSalt());
    }


    /**
     * 进入授权角色页
     */
    @GetMapping("/authRole/{userId}")
    public String authRole(@PathVariable("userId") Long userId, ModelMap mmap) {
        SysUser user = sysUserService.findUser(userId);
        // 获取用户所属的角色列表
        List<SysRole> userRoles = sysRoleService.selectRolesByUserId(userId);
        List<Long> roleIds = userRoles.stream().map(e->e.getRoleId()).collect(Collectors.toList());
        mmap.put("user", user);
        mmap.put("userRoles", roleIds);
        return prefix + "/authRole";
    }

    /**
     * 用户授权角色
     */
    @RequiresPermissions("system:user:role")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PostMapping("/authRole/insertAuthRole")
    @AjaxWrapper
    public void insertAuthRole(Long userId, Long[] roleIds) {
        if(roleIds != null && !Arrays.asList(roleIds).contains(1L))
            sysUserService.checkUserAllowed(new SysUser(userId),"取消授权");
        SysUser user = sysUserService.findUser(userId);
        boolean hasUserDept = UserInfoContextHelper.getLoginUser().hashScopePermission(PermissionConstants.User.ROLE,user.getDeptId());
        if(!hasUserDept)
            throw UserHasNotPermissionException.buildWithPermission(PermissionConstants.User.ROLE);

        List<Long> toSaveRoleIds = filterRoles(userId,Arrays.asList(roleIds));
        roleIds = toSaveRoleIds.toArray(new Long[]{});

        sysUserService.saveUserRoles(userId, roleIds);
    }

    /**
     * 用户状态修改
     */
    @RequiresPermissions(PermissionConstants.User.EDIT)
    @Log(title = "状态修改", businessType = BusinessType.UPDATE)
    @PostMapping("/changeStatus")
    @AjaxWrapper
    public int changeStatus(SysUser user) {
        sysUserService.checkUserAllowed(user,"修改状态");
        SysUser u = sysUserService.findUser(user.getUserId());
        UserInfoContextHelper.checkScopePermission(PermissionConstants.User.EDIT,u.getDeptId());
        u.setStatus(user.getStatus());
        sysUserService.saveUser(u);
        return 1;
    }

    @RequiresPermissions(PermissionConstants.User.UNLOCK)
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @PostMapping("/unlock")
    @AjaxWrapper
    public void unlock(Long userId) {
        sysUserService.resetLoginFail(userId);
    }

    /**
     * 有 PermissionConstants.User.ROLE 权限才能给其他人
     * @param userId
     * @param roleIds
     * @return
     */
    private List<Long> filterRoles(Long userId, List<Long> roleIds){
        //这里不能让用户保存的时候选择太多权限
        //这个用户以前就有的权限
        List<Long> dbRoles = sysRoleService.selectRolesByUserId(userId).stream().map(e->e.getRoleId()).collect(Collectors.toList());
        //给客户端操作的角色
        List<SysRole> toPageRoles = sysRoleService.selectAllVisibleRolesByUserId(UserInfoContextHelper.getCurrentUserId(),userId);

        //除去给客户端展示的，剩下保持不变
        dbRoles.removeAll(toPageRoles.stream().map(e->e.getRoleId()).collect(Collectors.toList()));
        //给客户端的检查有没有勾选
        if(roleIds != null && roleIds.size() > 0) {
            List<SysRole> requestRoles = sysRoleService.selectRolesByRoleIds(roleIds.toArray(new Long[]{}));
            List<Long> pageRoleIds = toPageRoles.stream().map(e->e.getRoleId()).collect(Collectors.toList());
            //判断用户有没有添加新的角色
            requestRoles.forEach(e -> {
                //防止越权
                if (pageRoleIds.contains(e.getRoleId())) {
                    dbRoles.add(e.getRoleId());
                }
            });
        }
        return dbRoles;
    }
}
