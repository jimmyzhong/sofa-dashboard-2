package xyz.lot.dashboard.manage.dao;

import xyz.lot.dashboard.manage.entity.SysDictType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictTypeDao extends MongoRepository<SysDictType, Long> {

    SysDictType findDictTypeByDictId(Long dictId);

    SysDictType findDictTypeByDictType(String dictType);

    List<SysDictType> findAllDictTypeByDictType(String dictType);

    int deleteDictTypeByDictId(Long ditcId);
}
