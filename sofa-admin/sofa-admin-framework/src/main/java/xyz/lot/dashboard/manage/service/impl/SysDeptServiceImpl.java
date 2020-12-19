package xyz.lot.dashboard.manage.service.impl;

import xyz.lot.db.mongo.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import xyz.lot.dashboard.common.constants.UserConstants;
import xyz.lot.dashboard.manage.dao.DeptDao;
import xyz.lot.dashboard.manage.dao.UserDao;
import xyz.lot.dashboard.manage.entity.SysDept;
import xyz.lot.dashboard.manage.entity.SysUser;
import xyz.lot.common.exception.BusinessException;
import xyz.lot.dashboard.manage.service.SysDeptService;
import xyz.lot.db.mongo.service.MongoRuntimeConfigService;
import xyz.lot.db.mongo.util.CriteriaUtil;
import xyz.lot.dashboard.common.domain.Ztree;
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
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Service
@Slf4j
public class SysDeptServiceImpl extends CrudBaseServiceImpl<Long,SysDept> implements SysDeptService {

    @Autowired
    private DeptDao deptDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoRuntimeConfigService mongoRuntimeConfigService;

    @Override
    public List<Long> selectAllDeptId() {
        List<SysDept> results = selectAll();
        List<Long> ll = new ArrayList<>();
        results.forEach(e->{
            ll.add(e.getDeptId());
        });
        return ll;
    }

    @Override
    public List<SysDept> selectDeptList(SysDept search) {
        List<SysDept> lists = super.selectList(search);
        sortDepts(lists);
        return lists;
    }

    @Transactional
    @Override
    public long remove(Long deptId) {
        mongoRuntimeConfigService.updateRealmUpdateTime();
        return super.remove(deptId);
    }

    private void sortDepts(List<SysDept> lists) {
        if(lists == null || lists.size() == 0)
            return;
        Collections.sort(lists, new Comparator<SysDept>() {
            @Override
            public int compare(SysDept e1, SysDept e2) {
                if(e1.getParentId() == null)
                    return -1;
                if(e2.getParentId() == null)
                    return 1;
                if(e1.getParentId() > e2.getParentId()) {
                    return 1;
                } else if(e1.getParentId() < e2.getParentId()) {
                    return -1;
                } else {
                    return e1.getOrderNum()-e2.getOrderNum();
                }
            }
        });
    }

    @Override
    public List<Ztree> selectDeptTree(SysDept sysDept) {
        List<SysDept> sysDeptList = selectList(sysDept);
        List<Ztree> ztrees = initZtree(sysDeptList);
        return ztrees;
    }

    @Override
    public List<Ztree> roleDeptTreeData(Long roleId) {
        Assert.notNull(roleId,"");
        List<Ztree> ztrees;
        List<SysDept> sysDeptList = selectList(null);
//        if (roleId != null) {
            List<SysDept> roleSysDeptList = selectRoleDeptTree(roleId);
            ztrees = initZtree(sysDeptList, roleSysDeptList == null ? null : roleSysDeptList.stream().map(e -> e.getDeptId()).collect(Collectors.toList()));
//        } else {
//            ztrees = initZtree(sysDeptList);
//        }
        return ztrees;
    }


    @Override
    public int selectDeptCount(Long parentId) {
        return deptDao.countByParentId(parentId);
    }

    @Override
    public void checkExistChildDept(Long deptId) {
        Assert.notNull(deptId,"");
        SysDept cur = deptDao.findByDeptId(deptId);
        Assert.notNull(cur,"");
        List<SysDept> ps = deptDao.findAllByParentId(deptId);
        if(ps != null && ps.size() >0){
            List<String> names = ps.stream().filter(e->!Boolean.TRUE.equals(e.getIsDelete())).map(e->e.getDeptName()).collect(Collectors.toList());
            if(names.size() > 0 ) {
                throw BusinessException.build("部门["+cur.getDeptName()+"]存在这些"+names+"子部门,无法删除");
            }
        }
    }

    @Override
    public void checkDeptExistUser(Long deptId) {
        List<SysUser> us = userDao.findAllByDeptId(deptId);
        if(us !=null && us.size() > 0) {
            List<String> names = us.stream().filter(e->!Boolean.TRUE.equals(e.getIsDelete())).map(e->e.getLoginName()).collect(Collectors.toList());
            if(names.size() > 0 ) {
                log.info("部门{}存在这些用户{}无法删除", deptId, names);
                throw BusinessException.build("部门[" + deptId + "]存在这些" + names + "用户,无法删除");
            }
        }
    }

    @Override
    @Transactional
    public int insertDept(SysDept sysDept) {
        Assert.notNull(sysDept,"");
        Assert.notNull(sysDept.getParentId(),"");
        if(sysDept.getParentId().longValue() < 0) {
            throw BusinessException.build("部门的父部门Id不能小于0");
        }
        if(sysDept.getParentId().longValue() > 0) {
            SysDept dbSysDept = deptDao.findByDeptId(sysDept.getParentId());
            if(dbSysDept == null)
                throw BusinessException.build("父部不门存在");
        }
        checkDeptNameUnique(sysDept);
        sysDept.setCreateTime(new Date());
        super.insert(sysDept);
        processRelations(null, sysDept);
        mongoRuntimeConfigService.updateRealmUpdateTime();
        return 0;
    }

    @Transactional
    @Override
    public int updateDept(SysDept sysDept) {
        Assert.notNull(sysDept,"");
        Assert.notNull(sysDept.getParentId(),"");
        if(sysDept.getParentId().longValue() < 0) {
            throw BusinessException.build("部门的父部门Id不能小于0");
        }
        if(sysDept.getParentId().longValue() > 0) {
            SysDept dbSysDept = deptDao.findByDeptId(sysDept.getParentId());
            if(dbSysDept == null)
                throw BusinessException.build("父部不门存在");
        }
        //不能将父部门移动到子部门下面
        Long deptId = sysDept.getDeptId();
        Long newParentId = sysDept.getParentId();
        SysDept originParent = deptDao.findByDeptId(sysDept.getDeptId());
        Long originParentId = originParent.getParentId() ;
        if(!originParentId.equals(newParentId)) {
            //修改了上级部门,这个上级部门不能是自己的子节点
            do {
                SysDept newParent = deptDao.findByDeptId(newParentId);
                if(newParent == null) {
                    break;
                }
                if (newParent!=null && newParent.getDeptId().equals(deptId)) {
                    throw BusinessException.build("修改了上级部门,这个上级部门不能是自己的子节点");
                }
                newParentId = newParent.getParentId();
            } while (true);
        }
        boolean isUnique = checkDeptNameUnique(sysDept);
        if (!isUnique)
            throw BusinessException.build("部门名称已存在");
        SysDept dbSysDept = deptDao.findByDeptId(sysDept.getDeptId());
        if(dbSysDept == null)
            throw BusinessException.build("部不门存在");

        sysDept.setId(dbSysDept.getId());
        processRelations(dbSysDept, sysDept);

        dbSysDept.setDeptName(sysDept.getDeptName());
        dbSysDept.setLeader(sysDept.getLeader());
        dbSysDept.setPhone(sysDept.getPhone());
        dbSysDept.setEmail(sysDept.getEmail());
        dbSysDept.setOrderNum(sysDept.getOrderNum());
        dbSysDept.setStatus(sysDept.getStatus());
        dbSysDept.setParentId(sysDept.getParentId());
        dbSysDept.setParentName(sysDept.getParentName());
        dbSysDept.setAncestors(sysDept.getAncestors());
        dbSysDept.setDescendents(sysDept.getDescendents());
        dbSysDept.setChildren(sysDept.getChildren());
        dbSysDept.setUpdateTime(new Date());

        deptDao.save(dbSysDept);
        return 0;
    }



    @Override
    public SysDept selectDeptByDeptId(Long deptId) {
        return deptDao.findByDeptId(deptId);
    }

    @Override
    public boolean checkDeptNameUnique(SysDept sysDept) {
        SysDept dd = deptDao.findByDeptNameAndParentId(sysDept.getDeptName(), sysDept.getParentId());
        if (dd != null && !dd.getDeptId().equals(sysDept.getDeptId()))
            return false;
        return true;
    }

    @Override
    public List<Ztree> selectDeptTreeData(Long[] deptIds) {
        List<SysDept> allSysDepts = selectAll();
        List<SysDept> roleSysDeptList = deptDao.findAllByDeptIdIn(deptIds);
        //加上了父节点，前端好展示
        List<SysDept> toShow = new ArrayList<>();
        if(deptsHasSameParent(roleSysDeptList)){
            toShow.addAll(roleSysDeptList);
        } else {
            for (SysDept d : roleSysDeptList) {
                if (!toShow.contains(d)) {
                    toShow.add(d);
                }
                d.getAncestors().forEach(e -> {
                    if (!toShow.stream().map(to -> to.getDeptId()).collect(Collectors.toList()).contains(e)) {
                        SysDept ta = allSysDepts.stream().filter(dp -> dp.getDeptId().equals(e)).collect(Collectors.toList()).get(0);
                        if (!toShow.contains(ta)) {
                            toShow.add(ta);
                        }
                    }
                });
            }
        }
        sortDepts(toShow);
        List<Ztree> ztrees = initZtree2(toShow, Arrays.asList(deptIds), true, null,true);
        return ztrees;
    }

    private boolean deptsHasSameParent(List<SysDept> sysDepts){
        if(true)
            return false;
        if(sysDepts ==null || sysDepts.size() == 1)
            return true;
        Long parentId = sysDepts.get(0).getParentId();
        if(parentId == null)
            return false;
        boolean isSame = true;
        for(SysDept d: sysDepts) {
            if (!parentId.equals(d.getParentId())) {
                isSame = false;
            }
        };
        return isSame;
    }

    /**
     * 用户对哪些部门有查询权限
     * @param userId
     * @return
     */
    private List<SysDept> selectUserDeptTree(Long userId) {
        List<AggregationOperation> aggregationOperations = new ArrayList<>();

        LookupOperation lookupOperationRoleDept = LookupOperation.newLookup().
                from("sys_role_dept").
                localField("deptId").
                foreignField("deptId").
                as("rd");
        aggregationOperations.add(lookupOperationRoleDept);

        List<AggregationOperation> subAggres = new ArrayList<>();
        LookupOperation lookupOperationUserRole = LookupOperation.newLookup().
                from("sys_user_role").
                localField("rd.roleId").
                foreignField("roleId").
                as("ur");
        subAggres.add(lookupOperationUserRole);

        LookupOperation lookupOperationRole = LookupOperation.newLookup().
                from("sys_role").
                localField("ur.roleId").
                foreignField("roleId").
                as("r");
        subAggres.add(lookupOperationRole);

//        LookupOperation lookupOperationDept = LookupOperation.newLookup().
//                from("sys_dept").
//                localField("deptId").
//                foreignField("deptId").
//                as("d");
//        subAggres.add(lookupOperationDept);


        Criteria ur_userId = Criteria.where("ur.userId");
        Criteria r_dataScope = Criteria.where("r.dataScope");

        Criteria con1 = r_dataScope.is("1");
        //Criteria con2 = ur_userId.is(userId).andOperator(r_dataScope.is("2"));

        subAggres.add(match(con1));




        //MatchOperation mt = match(Criteria.where("r.dataScope").is("2"));


        aggregationOperations.addAll(subAggres);
        //        aggregationOperations.add(match(Criteria.where("rd.roleId").is("ur.roleId")));
        aggregationOperations.add(Aggregation.match(CriteriaUtil.notDeleteCriteria()));

        Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);
        return mongoTemplate.aggregate(aggregation, SysDept.class, SysDept.class).getMappedResults();
    }


    /**
     * 角色 关联的部门id
     *
     * @param roleId
     * @return
     */
    private List<SysDept> selectRoleDeptTree(Long roleId) {
        List<AggregationOperation> aggregationOperations = new ArrayList<>();

        LookupOperation lookupOperation = LookupOperation.newLookup().
                from("sys_role_dept").
                localField("deptId").
                foreignField("deptId").
                as("ar");
        aggregationOperations.add(lookupOperation);
        aggregationOperations.add(match(Criteria.where("ar.roleId").is(roleId)));
        aggregationOperations.add(Aggregation.match(CriteriaUtil.notDeleteCriteria()));

        Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);
        return mongoTemplate.aggregate(aggregation, SysDept.class, SysDept.class).getMappedResults();

//        List<String> depts = new ArrayList<>();
//        if(results2 != null) {
//            Iterator<Document> it = results2.iterator();
//            while (it.hasNext()) {
//                Map<String,Object> resultsMap =  new HashMap<>();
//                Document doc = it.next();
//                doc.entrySet().forEach(e -> {
//                    resultsMap.put(e.getKey(),e.getValue());
//                });
//                if(resultsMap.get("ar") != null){
//                    String jsonStringUser = JSONObject.toJSONString(resultsMap.get("ar"));
//                    List<SysDept> ddds  = JSONObject.parseArray(jsonStringUser,SysDept.class);
//                    if(ddds != null && ddds.size() > 0 ){
//                        SysDept ddd = ddds.get(0);
//                        depts.add(ddd.getDeptId()+ddd.getDeptName());
//                    }
//                }
//            }
//        }
//
//        return depts;
    }

    private void processRelations(SysDept oldSysDept, SysDept newSysDept) {
        //刷新部门父子关系
        Long oldParentDeptId = oldSysDept == null ? null : oldSysDept.getParentId();
        Long currentDeptId = newSysDept.getDeptId();
        Long newParentDeptId = newSysDept.getParentId();
        List<SysDept> allSysDepts = deptDao.findAll();

        //记录后面准备update到数据库的节点
        List<SysDept> modifySysDepts = new ArrayList<>();

        //当前节点的所有父节点更新
        List<Long> newAnscents = new ArrayList<>();
        Long loopId = newParentDeptId;
        while (loopId != null) {
            SysDept parent = searchDept(allSysDepts, loopId);
            if (parent == null)
                break;
            if(!parent.getDeptId().equals(0L)) {
                newAnscents.add(parent.getDeptId());
                loopId = parent.getParentId();
            }
        }
        if (newAnscents.size() > 0) {
            newSysDept.setAncestors(newAnscents);
            modifySysDepts.add(newSysDept);
        }

        //当前节点的所有子孙节点
        newSysDept.getChildren().clear();
        newSysDept.getDescendents().clear();
        for (SysDept d : allSysDepts) {
            if (d.getParentId() != null && d.getParentId().equals(newSysDept.getDeptId())) {
                newSysDept.getChildren().add(d.getDeptId());
            }
            if (d.getAncestors() != null && d.getAncestors().contains(newSysDept.getDeptId())) {
                newSysDept.getDescendents().add(d.getDeptId());
            }
        }
        modifySysDepts.add(newSysDept);

        //当前修改的节点的所有子节点
        //allDesDeptIds 所有的子孙节点
        List<SysDept> allDesSysDepts = new ArrayList<>();
        List<Long> allDesDeptIds = newSysDept.getDescendents();
        allDesDeptIds.forEach(e -> {
            SysDept d = searchDept(allSysDepts, e);
            if (d != null) {
                allDesSysDepts.add(d);
            }
        });
        for (SysDept d : allDesSysDepts) {
            if (!modifySysDepts.contains(d))
                modifySysDepts.add(d);
            //ancestors 去掉oldParentId
            if (oldParentDeptId != null)
                d.getAncestors().remove(oldParentDeptId);
            //ancestors 增加newParentId
            if(!d.getAncestors().contains(newParentDeptId) && !newParentDeptId.equals(0L))
                d.getAncestors().add(newParentDeptId);
            //当前节点的所有子孙节点 去掉 祖先id
            List<Long> oldAcs = oldSysDept.getAncestors();
            if(oldAcs != null) {
                oldAcs.forEach(e -> {
                    if(d.getAncestors().contains(e))
                        d.getAncestors().remove(e);
                });
            }
            //当前节点的所有子孙节点 加上 祖先id
            List<Long> newAcs = newSysDept.getAncestors();
            if(newAcs != null) {
                newAcs.forEach(e -> {
                    if(!d.getAncestors().contains(e))
                        d.getAncestors().add(e);
                });
            }
        }

        //老的父节点Descendents都去掉allDesDeptIds
        if (oldParentDeptId != null) {
            Long cp = oldSysDept.getParentId();
            while (cp != null) {
                SysDept parentSysDept = searchDept(allSysDepts, cp);
                if (parentSysDept == null)
                    break;
                //直接父节点children去掉currentDeptId
                if (oldParentDeptId.equals(cp)) {
                    parentSysDept.getChildren().remove(currentDeptId);
                }
                parentSysDept.getDescendents().remove(currentDeptId);
                parentSysDept.getDescendents().removeAll(allDesDeptIds);
                if (!modifySysDepts.contains(parentSysDept))
                    modifySysDepts.add(parentSysDept);
                cp = parentSysDept.getParentId();
            }
        }
        //新的父节点Descendents都加上allDesDeptIds
        Long cp = newSysDept.getParentId();
        while (cp != null) {
            SysDept parentSysDept = searchDept(allSysDepts, cp);
            if (parentSysDept == null)
                break;
            //直接父节点children增加currentDeptId
            if (newParentDeptId.equals(cp)) {
                parentSysDept.getChildren().add(currentDeptId);
            }
            parentSysDept.getDescendents().add(currentDeptId);
            parentSysDept.getDescendents().addAll(allDesDeptIds);
            if (!modifySysDepts.contains(parentSysDept))
                modifySysDepts.add(parentSysDept);
            cp = parentSysDept.getParentId();
        }
        modifySysDepts.forEach(e ->{
            e.setUpdateTime(new Date());
        });
        deptDao.saveAll(modifySysDepts);
    }

    private SysDept searchDept(List<SysDept> sysDepts, Long deptId) {
        for (SysDept d : sysDepts) {
            if (d.getDeptId().equals(deptId)) {
                return d;
            }
        }
        return null;
    }

    /**
     * 对象转部门树
     *
     * @param sysDeptList 部门列表
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysDept> sysDeptList) {
        return initZtree(sysDeptList, null);
    }

    /**
     * 对象转部门树
     *
     * @param sysDeptList     部门列表
     * @param roleDeptList 角色已存在菜单列表
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysDept> sysDeptList, List<Long> roleDeptList) {
        List<Ztree> ztrees = new ArrayList<>();
        boolean isCheck = roleDeptList != null;
        for (SysDept sysDept : sysDeptList) {
            if (UserConstants.DEPT_NORMAL.equals(sysDept.getStatus())) {
                Ztree ztree = new Ztree();
                ztree.setId(sysDept.getDeptId());
                ztree.setPId(sysDept.getParentId());
                ztree.setName(sysDept.getDeptName());
                ztree.setTitle(sysDept.getDeptName());
                if (isCheck) {
                    ztree.setChecked(roleDeptList.contains(sysDept.getDeptId()));

                }
                ztrees.add(ztree);
            }
        }
        return ztrees;
    }

    public List<Ztree> initZtree2(List<SysDept> sysDeptList, List<Long> roleDeptList,
                                  Boolean check, Boolean open, Boolean nocheck) {
        List<Ztree> ztrees = new ArrayList<>();
        for (SysDept sysDept : sysDeptList) {
            if (UserConstants.DEPT_NORMAL.equals(sysDept.getStatus())) {
                Ztree ztree = new Ztree();
                ztree.setId(sysDept.getDeptId());
                ztree.setPId(sysDept.getParentId());
                ztree.setName(sysDept.getDeptName());
                ztree.setTitle(sysDept.getDeptName());
                boolean contains = roleDeptList.contains(sysDept.getDeptId());
                if(check != null && check.booleanValue())
                    ztree.setChecked(contains);
                if(open !=null && open.booleanValue())
                    ztree.setOpen(contains);
                if(nocheck != null && nocheck.booleanValue())
                    ztree.setNocheck(contains);
                ztrees.add(ztree);
            }
        }
        return ztrees;
    }

}
