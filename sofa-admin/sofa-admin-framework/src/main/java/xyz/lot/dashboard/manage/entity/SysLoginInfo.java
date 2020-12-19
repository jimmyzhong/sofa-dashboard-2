package xyz.lot.dashboard.manage.entity;

import xyz.lot.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.lot.common.annotation.AutoId;
import xyz.lot.common.annotation.Excel;
import xyz.lot.common.annotation.PrimaryId;
import xyz.lot.common.annotation.Search;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_login_info")
@Data
public class SysLoginInfo extends TimedBasedEntity {
    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @PrimaryId
    @Excel(name = "序号", cellType = Excel.ColumnType.NUMERIC)
    @AutoId
    @Indexed(unique = true)
    private Long infoId;

    /**
     * 用户账号
     */
    @Excel(name = "用户账号")
    @Search
    private String loginName;

    /**
     * 登录状态 0成功 1失败
     */
    @Excel(name = "登录状态", readConverterExp = "0=成功,1=失败")
    @Search
    private String status;

    /**
     * 登录IP地址
     */
    @Excel(name = "登录地址")
    @Search
    private String ipAddr;

    /**
     * 登录地点
     */
    @Excel(name = "登录地点")
    private String loginLocation;

    /**
     * 浏览器类型
     */
    @Excel(name = "浏览器")
    private String browser;

    /**
     * 操作系统
     */
    @Excel(name = "操作系统")
    private String os;

    /**
     * 提示消息
     */
    @Excel(name = "提示消息")
    private String msg;

    /**
     * 访问时间
     */
    @Excel(name = "访问时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("infoId", getInfoId())
                .append("loginName", getLoginName())
                .append("ipAddr", getIpAddr())
                .append("loginLocation", getLoginLocation())
                .append("browser", getBrowser())
                .append("os", getOs())
                .append("status", getStatus())
                .append("msg", getMsg())
                .append("loginTime", getLoginTime())
                .toString();
    }
}
