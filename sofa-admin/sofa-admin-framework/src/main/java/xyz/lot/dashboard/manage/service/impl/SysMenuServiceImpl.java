package xyz.lot.dashboard.manage.service.impl;

import lombok.extern.slf4j.Slf4j;
import xyz.lot.dashboard.manage.dao.RoleDao;
import xyz.lot.dashboard.manage.entity.SysRole;
import xyz.lot.db.mongo.service.CrudBaseServiceImpl;
import xyz.lot.dashboard.manage.dao.MenuDao;
import xyz.lot.dashboard.manage.dao.RoleMenuDao;
import xyz.lot.dashboard.manage.entity.SysMenu;
import xyz.lot.dashboard.manage.entity.SysRoleMenu;
import xyz.lot.common.exception.BusinessException;
import xyz.lot.dashboard.manage.service.SysMenuService;
import xyz.lot.db.mongo.service.MongoRuntimeConfigService;
import xyz.lot.db.mongo.util.CriteriaUtil;
import xyz.lot.dashboard.common.domain.Ztree;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Slf4j
@Service
public class SysMenuServiceImpl extends CrudBaseServiceImpl<Long,SysMenu> implements SysMenuService {

    @Autowired
    private MenuDao menuDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private RoleMenuDao roleMenuDao;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoRuntimeConfigService mongoRuntimeConfigService;
    /**
     * 首页菜单显示
     * @param userId
     * @return
     */
    @Override
    public List<SysMenu> selectVisibleMenusByUser(Long userId) {
        Assert.notNull(userId,"用户ID不能为空");
        List<SysMenu> sysMenus = doSelectPermsByUserId(userId, true);
        if(sysMenus != null && sysMenus.size() > 0) {
            sysMenus = sysMenus.stream().filter(e -> StringUtils.equals("0", e.getVisible()))
                    .filter(e -> e.getIsDelete() == null || !e.getIsDelete().booleanValue()).collect(toList());
        }
        sortMenus(sysMenus);
        return toTree(sysMenus, 0);
    }

    @Override
    public List<SysMenu> selectVisibleMenus() {
        List<SysMenu> sysMenus = doSelectPermsByUserId(null, true);
        if(sysMenus != null && sysMenus.size() > 0) {
            sysMenus = sysMenus.stream().filter(e -> StringUtils.equals("0", e.getVisible()))
                    .filter(e -> e.getIsDelete() == null || !e.getIsDelete().booleanValue()).collect(toList());
        }
        sortMenus(sysMenus);
        return toTree(sysMenus, 0);
    }

    /**
     * 返回正常菜单
     *
     * @return
     */
    @Override
    public List<SysMenu> selectMenusByUser(Long userId) {
        Assert.notNull(userId,"");
        List<SysMenu> sysMenus;
        // 管理员显示所有菜单信息
        if (userId == null || userId.equals(1L)) {
            sysMenus = doSelectPermsByUserId(null, true);
        } else {
            sysMenus = doSelectPermsByUserId(userId, true);
        }
        sortMenus(sysMenus);
        return getAllChildList(sysMenus, 0);
    }

    private void sortMenus(List<SysMenu> sysMenus) {
        if (sysMenus == null || sysMenus.size() == 0)
            return;
        Collections.sort(sysMenus, (o1, o2) -> {
            if(o1.getParentId() == null) {
                return -1;
            } else if(o2.getParentId() == null) {
                return 1;
            }
            if(o1.getParentId() == o2.getParentId()) {
                return o1.getOrderNum() - o2.getOrderNum();
            } else if (o1.getParentId() > o2.getParentId()) {
                return 1;
            } else {
                return -1;
            }
        });
    }

    @Override
    public List<SysMenu> selectMenuList(SysMenu sysMenu) {
        List<SysMenu> results = selectList(sysMenu);
        sortMenus(results);
        return results;
    }

    @Override
    public List<SysMenu> selectMenuAll() {
        List<SysMenu> sysMenus = selectAll();
        sortMenus(sysMenus);
        return sysMenus;
    }

    @Override
    public Set<String> selectPermsByUserId(Long userId) {
        //查询用户所有的权限 String格式
        List<SysMenu> perms = doSelectPermsByUserId(userId, false);
        Set<String> permsSet = new HashSet<>();
        for (SysMenu perm : perms) {
            if (StringUtils.isNotEmpty(perm.getPerms())) {
                permsSet.addAll(Arrays.asList(perm.getPerms().trim().split(",")));
            }
        }
        return permsSet;
    }

    @Override
    public Set<String> selectPermsAll() {
        //查询用户所有的权限 String格式
        List<SysMenu> perms = doSelectPermsByUserId(null, false);
        Set<String> permsSet = new HashSet<>();
        for (SysMenu perm : perms) {
            if (StringUtils.isNotEmpty(perm.getPerms())) {
                permsSet.addAll(Arrays.asList(perm.getPerms().trim().split(",")));
            }
        }
        return permsSet;
    }
    
    @Transactional
    @Override
    public long remove(Long menuId) throws BusinessException {
        if (selectCountMenuByParentId(menuId) > 0) {
            throw BusinessException.build("存在子菜单,不允许删除");
        }
        int usedCount = selectCountRoleMenuByMenuId(menuId);
        if (usedCount > 0) {
            boolean isRemove = true;
            List<SysRoleMenu> mrs = roleMenuDao.findAllByMenuId(menuId);
            List<String> noticeRoleNames = new ArrayList<>();
            for (SysRoleMenu e : mrs) {
                Long roleId = e.getRoleId();
                SysRole sr = roleDao.findByRoleId(roleId);
                if (sr == null) {
                    roleMenuDao.deleteAllByRoleId(roleId);
                } else {
                    String srn = sr.getRoleName();
                    if (!noticeRoleNames.contains(srn)) {
                        noticeRoleNames.add(srn);
                    }
                    isRemove = false;
                }
            }
            if (!isRemove) {
                throw BusinessException.build("菜单已分配给角色" + noticeRoleNames + ",不允许删除");
            }
        }
        //去掉role里面的引用
        roleMenuDao.deleteAllByMenuId(menuId);
        mongoRuntimeConfigService.updateRealmUpdateTime();
        return super.remove(menuId);
    }

    /**
     * role下面的菜单权限,给角色分配权限
     *
     * @param roleId 角色ID
     * @return
     */
    @Override
    public List<Ztree> roleMenuTreeData(Long roleId) {
        List<Ztree> ztrees = null;
        final List<SysMenu> sysMenuList = selectMenuList(null);

        SysMenu root = new SysMenu();
        root.setMenuId(0L);
        root.setMenuName("全部");
        root.setPerms("");
        sysMenuList.add(root);

        sortMenus(sysMenuList);
        //只显示正常的
//        sysMenuList = sysMenuList.stream().filter(e -> StringUtils.equals("0",e.getVisible()))
//                .filter(e -> e.getIsDelete() == null || !e.getIsDelete().booleanValue()).collect(toList());
        //获取删除，禁止节点
        List<SysMenu> unnormalList = sysMenuList.stream().filter(
                e -> StringUtils.equals("1",e.getVisible()) ||  e.getIsDelete() != null && e.getIsDelete().booleanValue()).collect(toList());

        //删除不该显示子节点
        List<SysMenu> unnormalChildList = new ArrayList<>();
        unnormalList.forEach( e -> {
            List<SysMenu> rmm = getAllChildList(sysMenuList,e.getMenuId());
            unnormalChildList.addAll(rmm);
        });

        sysMenuList.removeAll(unnormalList);
        sysMenuList.removeAll(unnormalChildList);

        if (roleId != null) {
            List<SysRoleMenu> sysRoleMenus = roleMenuDao.findAllByRoleId(roleId);
            List<Long> roleMenuList = sysRoleMenus == null ? null : sysRoleMenus.stream().map(e -> e.getMenuId()).collect(toList());
            ztrees = initZtree(sysMenuList, roleMenuList, true);
        } else {
            ztrees = initZtree(sysMenuList, null, true);
        }

        return ztrees;
    }

    @Override
    public List<String> selectPermsByRoleId(Long roleId) {
        Assert.notNull(roleId,"");
        List<SysRoleMenu> sysRoleMenus = roleMenuDao.findAllByRoleId(roleId);
        List<Long> menuIds = sysRoleMenus == null ? null : sysRoleMenus.stream().map(e -> e.getMenuId()).collect(toList());
        List<SysMenu> sysMenus = menuDao.findAllByMenuIdIn(menuIds);
        return sysMenus == null? null: sysMenus.stream().map(e -> e.getPerms()).filter(e -> StringUtils.isNotBlank(e)).collect(toList());
    }

    /**
     * 查询所有菜单
     *
     * @return 菜单列表
     */
    @Override
    public List<Ztree> menuTreeData() {
        List<SysMenu> sysMenuList = menuDao.findAll();

        SysMenu root = new SysMenu();
        root.setMenuId(0L);
        root.setMenuName("根目录");
        root.setPerms("");
        sysMenuList.add(root);

        sortMenus(sysMenuList);
        List<Ztree> ztrees = initZtree(sysMenuList);
        return ztrees;
    }

    /**
     * 对象转菜单树
     *
     * @param sysMenuList 菜单列表
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysMenu> sysMenuList) {
        return initZtree(sysMenuList, null, false);
    }

    /**
     * 对象转菜单树
     *
     * @param sysMenuList     菜单列表,所有的菜单
     * @param roleMenuList 角色已存在菜单列表，部分菜单
     * @param permsFlag    是否需要显示权限标识
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysMenu> sysMenuList, List<Long> roleMenuList, boolean permsFlag) {
        List<Ztree> ztrees = new ArrayList<>();
        for (SysMenu sysMenu : sysMenuList) {
            Ztree ztree = new Ztree();
            ztree.setId(sysMenu.getMenuId());
            ztree.setPId(sysMenu.getParentId());
            ztree.setName(transMenuName(sysMenu,permsFlag));
            ztree.setTitle(sysMenu.getMenuName());
            if (roleMenuList != null) {
                ztree.setChecked(roleMenuList.contains(sysMenu.getMenuId()));
            }
            ztrees.add(ztree);
        }
        return ztrees;
    }

    public String transMenuName(SysMenu sysMenu, boolean permsFlag) {
        StringBuffer sb = new StringBuffer();
        sb.append(sysMenu.getMenuName());
        if (permsFlag) {
            sb.append("<font color=\"#888\">&nbsp;&nbsp;&nbsp;" + sysMenu.getPerms() + "</font>");
        }
        return sb.toString();
    }

    @Override
    public SysMenu selectMenuById(Long menuId) {
        SysMenu m = selectByPId(menuId);
        if (m != null) {
            SysMenu parentSysMenu = menuDao.findByMenuId(m.getParentId());
            if (parentSysMenu != null) {
                m.setParentName(parentSysMenu.getMenuName());
            }
        }
        return m;
    }

    @Override
    public int selectCountMenuByParentId(Long parentId) {
        return menuDao.countByParentId(parentId);
    }

    @Override
    public int selectCountRoleMenuByMenuId(Long menuId) {
        return roleMenuDao.countByMenuId(menuId);
    }

    @Override
    public int insertMenu(SysMenu sysMenu) {
        checkMenuValid(sysMenu);
        super.insert(sysMenu);
        return 1;
    }

    @Override
    public int updateMenu(SysMenu sysMenu) {
        Assert.notNull(sysMenu, "");
        Assert.notNull(sysMenu.getMenuId(), "menuId不能未空");
        SysMenu dbSysMenu = menuDao.findByMenuId(sysMenu.getMenuId());
        if (dbSysMenu != null) {
            sysMenu.setId(dbSysMenu.getId());
        } else {
            throw BusinessException.build("菜单未找到 menuId=" + sysMenu.getMenuId());
        }
        checkMenuValid(sysMenu);
        menuDao.save(sysMenu);
        return 1;
    }

    private void checkMenuValid(SysMenu m) throws BusinessException {
        if(!checkMenuNameUnique(m))
            throw BusinessException.build("菜单名称重复");
        if(StringUtils.isBlank(m.getMenuType())){
            throw BusinessException.build("菜单类型不能为空");
        }
        if(StringUtils.equalsAny(m.getMenuType(),"C","F") && StringUtils.isBlank(m.getPerms())){
            throw BusinessException.build("权限标识不能为空");
        }
//        if(StringUtils.isBlank(m.getOrderNum())){
//            throw BusinessException.build("显示排序不能为空");
//        }
        if(!StringUtils.equalsAny(m.getMenuType(),"M","C","F")) {
            throw BusinessException.build("菜单类型不正确");
        }
        if(StringUtils.equalsAny(m.getMenuType(),"M","C")) {
            if(StringUtils.isBlank(m.getVisible())) {
                throw BusinessException.build("菜单菜单状态不能为空");
            }
            if(StringUtils.equals(m.getMenuType(),"C") && StringUtils.isBlank(m.getTarget())) {
                throw BusinessException.build("打开方式不能为空");
            }
        }

        if(m.getParentId() > 0) {
            SysMenu parentMenu = menuDao.findByMenuId(m.getParentId());
            if(parentMenu == null) {
                throw BusinessException.build("父菜单不存在");
            }
            if(parentMenu.getMenuId().equals(m.getMenuId())) {
                throw BusinessException.build("当前菜单的父菜单不能是自己");
            }
            if(StringUtils.equalsAny(m.getMenuType(),"M","C")) {
                if (!StringUtils.equals(parentMenu.getMenuType(), "M")) {
                    throw BusinessException.build("父菜单不是目录，不能选择");
                }
            } else if(StringUtils.equals(m.getMenuType(),"F")) {
                if (StringUtils.equals(parentMenu.getMenuType(), "F")) {
                    throw BusinessException.build("父菜单不是目录或者菜单，不能选择");
                }
            }
        }

        List<SysMenu> childs = menuDao.findAllByParentId(m.getMenuId());
        if(childs != null && childs.size() > 0) {
            if(!StringUtils.equalsAny(m.getMenuType(),"M","C")) {
                throw BusinessException.build("菜单有子成员，类型只能修改为目录或者菜单，不能修改为其他类型");
            }
        }

    }

    @Override
    public boolean checkMenuNameUnique(SysMenu sysMenu) {
        Assert.notNull(sysMenu, "sysMenu 不能为空");
        Assert.notNull(sysMenu.getMenuName(), "menuName 不能为空");
        Long parentId = sysMenu.getParentId() == null ? 0L : sysMenu.getParentId();
        SysMenu m = menuDao.findByMenuName(sysMenu.getMenuName());
        if(m!= null && m.getMenuId().equals(sysMenu.getMenuId())) {
            return true;
        }else if (m != null && !m.getParentId().equals(parentId)) {
            return false;
        }
        return true;
    }

    /**
     * 用户userId所拥有的权限
     *
     * @param userId
     * @param menuOnly 只显示菜单，不显示按钮
     * @return
     */
    private List<SysMenu> doSelectPermsByUserId(Long userId, boolean menuOnly) {

        List<AggregationOperation> aggregationOperations = new ArrayList<>();

        String sys_role_menu_prefix = "sys_role_menu.";
        LookupOperation lookupOperation = LookupOperation.newLookup().
                from("sys_role_menu").
                localField("menuId").
                foreignField("menuId").
                as("sys_role_menu");
        aggregationOperations.add(lookupOperation);

        String sys_user_role_prefix = "sys_user_role.";
        LookupOperation lookupOperationUserMenu = LookupOperation.newLookup().
                from("sys_user_role").
                localField(sys_role_menu_prefix + "roleId").
                foreignField("roleId").
                as("sys_user_role");
        aggregationOperations.add(lookupOperationUserMenu);

        //查询某个用户的权限
        if (userId != null)
            aggregationOperations.add(match(Criteria.where(sys_user_role_prefix + "userId").is(userId)));

        //只显示正常
        if (menuOnly) {
            aggregationOperations.add(match(Criteria.where("menuType").in(new String[]{"M", "C"})));
        } else {
            aggregationOperations.add(match(Criteria.where("menuType").in(new String[]{"M", "C", "F"})));
        }

        aggregationOperations.add(match(Criteria.where("visible").is("0")));
        aggregationOperations.add(match(CriteriaUtil.notDeleteCriteria()));

        //aggregationOperations.add(skip(start));
        //aggregationOperations.add(limit(size));

        Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);
        //List<Document> results3 = mongoTemplate.aggregate(aggregation, SysMenu.class, Document.class).getMappedResults();
        List<SysMenu> dbSysMenus = mongoTemplate.aggregate(aggregation, SysMenu.class, SysMenu.class).getMappedResults();
        return new ArrayList<SysMenu>() {{
            addAll(dbSysMenus);
        }};

//        List<SysMenu> perms = new ArrayList<>();
//        if(results2 != null) {
//            Iterator<Document> it = results2.iterator();
//            while (it.hasNext()) {
//                Map<String,Object> resultsMap =  new HashMap<>();
//                Document doc = it.next();
//                doc.entrySet().forEach(e -> {
//                    resultsMap.put(e.getKey(),e.getValue());
//                });
//                String jsonStringUser = JSONObject.toJSONString(resultsMap);
//                SysMenu m2  = JSONObject.parseObject(jsonStringUser,SysMenu.class);
//                m2.setMenuId((Long) resultsMap.get("_id"));
//                perms.add(m2);
//            }
//        }

//        return perms;
    }

    public static List<SysMenu> getAllChildList(List<SysMenu> list, long parentId) {
        List<SysMenu> returnList = new ArrayList<>();
        for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext(); ) {
            SysMenu t = iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() != null && t.getParentId() == parentId) {
                recursionFn(list, t, returnList);
                if(!returnList.contains(t))
                    returnList.add(t);
            }
        }
        return returnList;
    }

    private static void recursionFn(List<SysMenu> list, SysMenu t, List<SysMenu> returnList) {
        // 得到子节点列表
        List<SysMenu> childList = getDirectChildList(list, t);
        if(childList == null || childList.size() == 0)
            return;
        t.setChildren(childList);
        childList.forEach(e->{
            if(!returnList.contains(e))
                returnList.add(e);
        });
        for (SysMenu tChild : childList) {
            recursionFn(list, tChild, returnList);
        }
    }

    /**
     * 得到子节点列表
     */
    private static List<SysMenu> getDirectChildList(List<SysMenu> list, SysMenu t) {

        List<SysMenu> tlist = new ArrayList<>();
        Iterator<SysMenu> it = list.iterator();
        while (it.hasNext()) {
            SysMenu n = it.next();
            if (n.getParentId() !=null && n.getParentId().longValue() == t.getMenuId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    public static List<SysMenu> toTree(List<SysMenu> list, long parentId) {
        List<SysMenu> returnList = new ArrayList<>();
        for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext(); ) {
            SysMenu t = iterator.next();
            if (t.getParentId() != null && t.getParentId() == parentId) {
                recursionTreeFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    private static void recursionTreeFn(List<SysMenu> list, SysMenu t) {
        // 得到子节点列表
        List<SysMenu> childList = getDirectChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList) {
            recursionTreeFn(list, tChild);
        }
    }

    /**
     * 判断是否有子节点
     */
    private static boolean hasChild(List<SysMenu> list, SysMenu t) {
        return getDirectChildList(list, t).size() > 0 ? true : false;
    }
}
