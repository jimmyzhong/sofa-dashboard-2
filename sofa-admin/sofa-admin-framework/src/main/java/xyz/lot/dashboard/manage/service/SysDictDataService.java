package xyz.lot.dashboard.manage.service;

import xyz.lot.db.common.service.CrudBaseService;
import xyz.lot.dashboard.manage.entity.SysDictData;

import java.util.List;

public interface SysDictDataService extends CrudBaseService<Long, SysDictData> {

    List<SysDictData> selectDictDataByType(String dictType);

    List<SysDictData> selectNormalDictDataByType(String dictType);

    String selectDictLabel(String dictType, String dictValue);

}
