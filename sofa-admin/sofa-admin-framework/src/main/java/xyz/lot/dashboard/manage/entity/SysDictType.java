package xyz.lot.dashboard.manage.entity;

import xyz.lot.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.lot.common.annotation.AutoId;
import xyz.lot.common.annotation.Excel;
import xyz.lot.common.annotation.PrimaryId;
import xyz.lot.common.annotation.Search;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_dict_type")
@Data
public class SysDictType extends TimedBasedEntity {

    @PrimaryId
    @Indexed(unique = true)
    @AutoId
    @Excel(name = "字典主键", cellType = Excel.ColumnType.NUMERIC)
    private Long dictId;

    @Excel(name = "字典名称")
    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "字典名称不能为空")
    @Size(min = 0, max = 100, message = "字典类型名称长度不能超过100个字符")
    private String dictName;

    @Excel(name = "字典类型")
    @Search
    @NotBlank(message = "字典类型不能为空")
    @Size(min = 0, max = 100, message = "字典类型类型长度不能超过100个字符")
    private String dictType;

    /**
     * 状态（0正常 1停用）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
}
