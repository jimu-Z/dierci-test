package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户权限管理对象 agri_user_access_grant
 *
 * @author ruoyi
 */
public class AgriUserAccessGrant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键", cellType = ColumnType.NUMERIC)
    private Long grantId;

    @Excel(name = "用户账号")
    private String userName;

    @Excel(name = "用户昵称")
    private String nickName;

    @Excel(name = "角色权限标识")
    private String roleKey;

    @Excel(name = "数据权限范围")
    private String dataScope;

    @Excel(name = "菜单可见范围")
    private String menuScope;

    @Excel(name = "授权状态", readConverterExp = "0=待审核,1=已授权,2=已驳回")
    private String grantStatus;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getGrantId()
    {
        return grantId;
    }

    public void setGrantId(Long grantId)
    {
        this.grantId = grantId;
    }

    @NotBlank(message = "用户账号不能为空")
    @Size(max = 64, message = "用户账号长度不能超过64个字符")
    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    @Size(max = 64, message = "用户昵称长度不能超过64个字符")
    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    @NotBlank(message = "角色权限标识不能为空")
    @Size(max = 100, message = "角色权限标识长度不能超过100个字符")
    public String getRoleKey()
    {
        return roleKey;
    }

    public void setRoleKey(String roleKey)
    {
        this.roleKey = roleKey;
    }

    @NotBlank(message = "数据权限范围不能为空")
    @Size(max = 32, message = "数据权限范围长度不能超过32个字符")
    public String getDataScope()
    {
        return dataScope;
    }

    public void setDataScope(String dataScope)
    {
        this.dataScope = dataScope;
    }

    @Size(max = 100, message = "菜单可见范围长度不能超过100个字符")
    public String getMenuScope()
    {
        return menuScope;
    }

    public void setMenuScope(String menuScope)
    {
        this.menuScope = menuScope;
    }

    public String getGrantStatus()
    {
        return grantStatus;
    }

    public void setGrantStatus(String grantStatus)
    {
        this.grantStatus = grantStatus;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("grantId", getGrantId())
            .append("userName", getUserName())
            .append("nickName", getNickName())
            .append("roleKey", getRoleKey())
            .append("dataScope", getDataScope())
            .append("menuScope", getMenuScope())
            .append("grantStatus", getGrantStatus())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
