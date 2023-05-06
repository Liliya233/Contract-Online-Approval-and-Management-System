package com.liliya.contract.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@TableName("department")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Department implements Serializable {
    @TableId(value = "department_id", type = IdType.AUTO)
    private Integer id;

    @TableField("department_name")
    private String name;

    @TableField("department_permission_admin")
    private boolean permissionAdmin;

    @TableField("department_permission_create")
    private boolean permissionCreate;

    @TableField("department_permission_approval")
    private Integer permissionApproval;

    @TableField("department_permission_seal")
    private boolean permissionSeal;

    @TableField("department_permission_terminate")
    private boolean permissionTerminate;

    @TableField("department_permission_access")
    private Integer permissionAccess;
}
