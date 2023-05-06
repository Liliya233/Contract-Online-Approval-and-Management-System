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

@TableName("user")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_name")
    private String name;

    @TableField("user_password")
    private String password;

    @TableField("user_email")
    private String email;

    @TableField("user_department_id")
    private Integer departmentId;
}
