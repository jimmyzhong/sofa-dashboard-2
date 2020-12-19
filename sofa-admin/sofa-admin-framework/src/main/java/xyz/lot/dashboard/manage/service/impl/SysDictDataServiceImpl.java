package xyz.lot.dashboard.manage.service.impl;

import xyz.lot.db.mongo.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import xyz.lot.dashboard.manage.dao.DictDataDao;
import xyz.lot.dashboard.manage.entity.SysDictData;
import xyz.lot.dashboard.manage.service.SysDictDataService;
import xyz.lot.db.mongo.util.CriteriaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class SysDictDataServiceImpl extends CrudBaseServiceImpl<Long, SysDictData> implements SysDictDataService {

    @Autowired
    private DictDataDao dictDataDao;

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        return dictDataDao.findAllByDictTypeOrderByDictSortAsc(dictType);
    }

    @Override
    public List<SysDictData> selectNormalDictDataByType(String dictType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("dictType").is(dictType));
        query.addCriteria(Criteria.where("status").is("0"));
        query.addCriteria(CriteriaUtil.notDeleteCriteria());
        return mongoTemplate.find(query, SysDictData.class);
    }

    @Override
    public String selectDictLabel(String dictType, String dictValue) {
        SysDictData sysDictData = dictDataDao.findByDictTypeAndDictValue(dictType, dictValue);
        if (sysDictData != null) {
            return sysDictData.getDictLabel();
        }
        return "";
    }

}
