package com.liliya.contract.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@TableName("contract")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Contract {
    @TableId(value = "contract_id", type = IdType.AUTO)
    private Integer id;

    @TableField("contract_name")
    private String name;

    @TableField("contract_author_id")
    private Integer authorId;

    @TableField("contract_status")
    private Integer status;

    @TableField("contract_create_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField("contract_type")
    private Integer type;

    @TableField("contract_start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startDate;

    @TableField("contract_end_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endDate;

    @TableField("contract_period_start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date periodStartDate;

    @TableField("contract_period_expire_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date periodExpireDate;

    @TableField("contract_party")
    private String party;

    @TableField("contract_funds")
    private Long funds;

    @TableField("contract_seal_author_id")
    private Integer sealAuthorId;

    @TableField("contract_seal_note")
    private String sealNote;

    @TableField("contract_end_note")
    private String endNote;
}
