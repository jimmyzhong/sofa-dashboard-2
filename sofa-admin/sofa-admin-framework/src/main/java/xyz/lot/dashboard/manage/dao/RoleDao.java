package xyz.lot.dashboard.manage.dao;

import xyz.lot.dashboard.manage.entity.SysRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleDao extends MongoRepository<SysRole, Long> {


    SysRole findByRoleName(String roleName);

    SysRole findByRoleKey(String roleKey);

    SysRole findByRoleId(Long roleId);

    List<SysRole> findAllByRoleIdIn(List<Long> roleId);

    List<SysRole> findAllByRoleIdIn(Long[] ids);
}
