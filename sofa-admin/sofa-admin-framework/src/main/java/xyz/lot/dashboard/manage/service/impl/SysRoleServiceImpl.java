package xyz.lot.dashboard.manage.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import xyz.lot.common.exception.BusinessException;
import xyz.lot.common.util.Convert;
import xyz.lot.dashboard.manage.dao.RoleDao;
import xyz.lot.dashboard.manage.dao.RoleDeptDao;
import xyz.lot.dashboard.manage.dao.RoleMenuDao;
import xyz.lot.dashboard.manage.dao.UserRoleDao;
import xyz.lot.dashboard.manage.entity.*;
import xyz.lot.dashboard.manage.service.SysDeptService;
import xyz.lot.dashboard.manage.service.SysRoleService;
import xyz.lot.dashboard.manage.service.SysUserService;
import xyz.lot.db.mongo.service.CrudBaseServiceImpl;
import xyz.lot.db.mongo.service.MongoRuntimeConfigService;
import xyz.lot.db.mongo.util.CriteriaUtil;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Service
public class SysRoleServiceImpl extends CrudBaseServiceImpl<Long,SysRole> implements SysRoleService {

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private RoleMenuDao roleMenuDao;

    @Autowired
    private RoleDeptDao roleDeptDao;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private MongoRuntimeConfigService mongoRuntimeConfigService;

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectRoleKeys(Long userId) {
        List<SysRole> sysRoles = findAllByUserId(userId);
        Set<String> permsSet = new HashSet<>();

        List<Long> roleIds = sysRoles.stream().map(e -> e.getRoleId()).collect(Collectors.toList());

        Iterable<SysRole> perms = roleDao.findAllByRoleIdIn(roleIds);
        for (SysRole perm : perms) {
            if (perm != null) {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    @Override
    public List<Long> selectDeptIdsByRoleId(Long roleId) {
        Assert.notNull(roleId,"");
        List<SysRoleDept> rds = roleDeptDao.findAllByRoleId(roleId);
        return rds == null ? null : rds.stream().map(e -> e.getDeptId()).collect(Collectors.toList());
    }

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 所有角色列表，用户有权限的打勾
     */
    @Override
    public List<SysRole> selectAllRolesByUserId(Long userId) {
        List<SysRole> userSysRoles = findAllByUserId(userId);
        List<SysRole> sysRoles = selectAll();
        for (SysRole sysRole : sysRoles) {
            for (SysRole userSysRole : userSysRoles) {
                if (sysRole.getRoleId().longValue() == userSysRole.getRoleId().longValue()) {
                    sysRole.setFlag(true);
                    break;
                }
            }
        }
        //返回的是系统所有的角色,用户有权限的节点会打勾
        return sysRoles;
    }

    @Override
    public List<SysRole> selectAllVisibleRolesByUserId(Long currentUserId, Long userId) {
        SysUser loginUser = sysUserService.findUser(currentUserId);

        List<SysRole> sysRoles = selectAll();
        //过滤
        sysRoles = sysRoles.stream().filter(e->{
            String visible = e.getVisibleScope();
            if(StringUtils.equals("1",visible)) {
                return true;
            } else if(StringUtils.equals("2",visible) ){
                //部门可见
                Long roleDepId = e.getDeptId();
                Long userDepId = loginUser.getDeptId();
                if(roleDepId!=null && userDepId !=null && roleDepId.equals(userDepId)) {
                    return true;
                } else {
                    return false;
                }
            } else if(StringUtils.equals("3",visible) ){
                //部门父部门可见,  登录用户的部门在角色的任何一个父部门
                Long roleDepId = e.getDeptId();
                Long userDepId = loginUser.getDeptId();
                if(roleDepId!=null && userDepId !=null ) {
                    if(roleDepId.equals(userDepId))
                        return true;
                    SysDept parent = sysDeptService.selectDeptByDeptId(roleDepId);
                    if(parent != null && parent.getAncestors() !=null && parent.getAncestors().contains(userDepId)) {
                        return true;
                    }
                }
            }
            return false;
        }).collect(Collectors.toList());


        if(userId != null) {
            List<SysRole> userSysRoles = findAllByUserId(userId);
            for (SysRole sysRole : sysRoles) {
                for (SysRole userSysRole : userSysRoles) {
                    if (sysRole.getRoleId().longValue() == userSysRole.getRoleId().longValue()) {
                        sysRole.setFlag(true);
                        break;
                    }
                }
            }
        }
        //返回的是系统所有的角色,用户有权限的节点会打勾
        return sysRoles;
    }

    @Override
    public List<SysRole> selectRolesByRoleIds(Long[] ids) {
        Assert.notNull(ids,"");
        return roleDao.findAllByRoleIdIn(ids);
    }

    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        return findAllByUserId(userId);
    }

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    @Transactional
    public long deleteRoleById(Long roleId) {
        long c = removeRoleInfo(roleId + "");
        mongoRuntimeConfigService.updateRealmUpdateTime();
        return c;
    }

    /**
     * 批量删除角色信息
     *
     * @param ids 需要删除的数据ID
     * @throws Exception
     */
    @Override
    @Transactional
    public long removeRoleInfo(String ids) throws BusinessException {
        Long[] roleIds = Convert.toLongArray(ids);
        for (Long roleId : roleIds) {
            SysRole sysRole = selectByPId(roleId);
            checkRoleAllowed(sysRole);
            if (countUserRoleByRoleId(roleId) > 0) {
                throw BusinessException.build(String.format("%1$s已分配,不能删除", sysRole.getRoleName()));
            }
            roleMenuDao.deleteAllByRoleId(roleId); // 删除角色与菜单关联
        }
        long c = super.removeByPIds(ids);
        mongoRuntimeConfigService.updateRealmUpdateTime();
        return c;
    }

    /**
     * 新增保存角色信息
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertRole(SysRole sysRole) {
        // 新增角色信息
        super.insert(sysRole);
        int c = insertRoleMenu(sysRole.getRoleId(), Arrays.asList(sysRole.getMenuIds()));
        mongoRuntimeConfigService.updateRealmUpdateTime();
        return c;
    }

    /**
     * 修改保存角色信息
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateRole(SysRole sysRole) {
        Assert.notNull(sysRole.getRoleId(), "roleId cant be null");
        // 修改角色信息
        roleDao.save(sysRole);
        // 删除角色与菜单关联
        roleMenuDao.deleteAllByRoleId(sysRole.getRoleId());
        //新增role -- menu
        int c = insertRoleMenu(sysRole.getRoleId(), Arrays.asList(sysRole.getMenuIds()));
        mongoRuntimeConfigService.updateRealmUpdateTime();
        return c;
    }

    /**
     * 修改数据权限信息
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int authDataScope(SysRole sysRole) {
        Assert.notNull(sysRole.getRoleId(), "roleId cant be null");
        Assert.notNull(sysRole.getDataScope(), "roleId cant be null");
        SysRole dbSysRole = roleDao.findByRoleId(sysRole.getRoleId());
        //dbSysRole.setRoleName(sysRole.getRoleName());
        //dbSysRole.setRoleKey(sysRole.getRoleKey());
        dbSysRole.setDataScope(sysRole.getDataScope());
        // 修改角色信息
        roleDao.save(dbSysRole);
        // 删除角色与部门关联
        roleDeptDao.deleteAllByRoleId(sysRole.getRoleId());
        // 新增角色和部门信息（数据权限）
        int c = insertRoleDept(sysRole.getRoleId(), Arrays.asList(sysRole.getDeptIds()));
        mongoRuntimeConfigService.updateRealmUpdateTime();
        return c;
    }

    /**
     * 校验角色名称是否唯一
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleNameUnique(SysRole sysRole) {
        Long roleId = sysRole.getRoleId() == null ? -1L : sysRole.getRoleId();
        SysRole info = roleDao.findByRoleName(sysRole.getRoleName());
        if (info != null && info.getRoleId().longValue() != roleId.longValue()) {
            return false;
        }
        return true;

    }

    /**
     * 校验角色权限是否唯一
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleKeyUnique(SysRole sysRole) {
        Long roleId = sysRole.getRoleId() == null ? -1L : sysRole.getRoleId();
        SysRole info = roleDao.findByRoleKey(sysRole.getRoleKey());
        if (info != null && info.getRoleId().longValue() != roleId.longValue()) {
            return false;
        }
        return true;
    }

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public int countUserRoleByRoleId(Long roleId) {
        return userRoleDao.countByRoleId(roleId);
    }

    /**
     * 角色状态修改
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    @Override
    public int changeStatus(SysRole sysRole) {
        SysRole dbSysRole = roleDao.findByRoleId(sysRole.getRoleId());
        dbSysRole.setStatus(sysRole.getStatus());
        roleDao.save(dbSysRole);
        return 1;
    }

    /**
     * 取消授权用户角色
     *
     * @param sysUserRole 用户和角色关联信息
     * @return 结果
     */
    @Override
    public long deleteAuthUser(SysUserRole sysUserRole) {
        Assert.notNull(sysUserRole.getUserId(), "userId cant be null");
        Assert.notNull(sysUserRole.getRoleId(), "roleId cant be null");
        if(sysUserRole.getRoleId().equals(1L)) {
            sysUserService.checkUserAllowed(new SysUser(sysUserRole.getUserId()), "取消授权");
        }
        return userRoleDao.deleteAllByUserIdAndRoleId(sysUserRole.getUserId(), sysUserRole.getRoleId());
    }

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    @Override
    public long deleteAuthUsers(Long roleId, String userIds) {
        Assert.notNull(roleId, "roleId cant be null");
        if(roleId.equals(1L)) {
            List<Long> uids = Convert.toLongList(userIds);
            uids.forEach(e -> {
                sysUserService.checkUserAllowed(new SysUser(e), "取消授权");
            });
        }
        List<SysUserRole> sysUserRoles = userRoleDao.findAllByRoleIdAndUserIdIn(roleId, Convert.toLongArray(userIds));
        userRoleDao.deleteAll(sysUserRoles);
        return sysUserRoles.size();
    }

    @Override
    public long deleteAuthUsers(List<Long> roleIds, Long userId) {
        sysUserService.checkUserAllowed(new SysUser(userId),"取消授权");
        List<SysUserRole> sysUserRoles = userRoleDao.findAllByRoleIdInAndUserId(roleIds, userId);
        userRoleDao.deleteAll(sysUserRoles);
        return sysUserRoles.size();
    }

    @Override
    public long deleteAuthUsers(Long userId) {
        sysUserService.checkUserAllowed(new SysUser(userId),"取消授权");
        return userRoleDao.deleteAllByUserId(userId);
    }

    /**
     * 批量选择授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    @Override
    public int insertAuthUsers(Long roleId, String userIds) {
        Long[] users = Convert.toLongArray(userIds);
        // 新增用户与角色管理
        List<SysUserRole> list = new ArrayList<SysUserRole>();
        for (Long userId : users) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleDao.saveAll(list).size();
    }

    @Override
    public void checkRoleAllowed(SysRole sysRole) {
        if (sysRole.getRoleId() !=null  && sysRole.isAdmin()) {
            throw BusinessException.build("不允许操作超级管理员角色");
        }
    }

    /**
     * 新增角色菜单信息
     *
     * @param roleId  角色对象
     * @param menuIds 角色拥有的菜单ID
     */
    private int insertRoleMenu(Long roleId, List<Long> menuIds) {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenu> list = new ArrayList<>();
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0) {
            List<SysRoleMenu> rm = roleMenuDao.saveAll(list);
            rows = rm.size();
        }
        return rows;
    }

    /**
     * 新增角色部门信息(数据权限)
     *
     * @param roleId
     * @param deptIds
     * @return
     */
    private int insertRoleDept(Long roleId, List<Long> deptIds) {
        int rows = 1;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDept> list = new ArrayList<SysRoleDept>();
        for (Long deptId : deptIds) {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(roleId);
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (list.size() > 0) {
            List<SysRoleDept> sysRoleDepts = roleDeptDao.saveAll(list);
            rows = sysRoleDepts.size();
        }
        return rows;
    }


    /**
     * 查找用户拥有的角色,返回所有
     *
     * @param userId
     * @return
     */
    private List<SysRole> findAllByUserId(Long userId) {
        Assert.notNull(userId, "");

        List<AggregationOperation> aggregationOperations = new ArrayList<>();

        LookupOperation lookupOperationUserMenu = LookupOperation.newLookup().
                from("sys_user_role").
                localField("roleId").
                foreignField("roleId").
                as("ur");
        aggregationOperations.add(lookupOperationUserMenu);


        LookupOperation lookupOperationSysUser = LookupOperation.newLookup().
                from("sys_user").
                localField("ur.userId").
                foreignField("userId").
                as("u");
        aggregationOperations.add(lookupOperationSysUser);

        LookupOperation lookupOperationSysDept = LookupOperation.newLookup().
                from("sys_dept").
                localField("u.deptId").
                foreignField("deptId").
                as("d");
        aggregationOperations.add(lookupOperationSysDept);

        //查询某个用户的权限
        aggregationOperations.add(match(Criteria.where("ur.userId").is(userId)));

        //只显示正常
        aggregationOperations.add(match(CriteriaUtil.notDeleteCriteria()));


        Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);
        return mongoTemplate.aggregate(aggregation, SysRole.class, SysRole.class).getMappedResults();
    }
}
