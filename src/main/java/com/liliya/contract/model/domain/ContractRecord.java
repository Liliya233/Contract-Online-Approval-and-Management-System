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

@TableName("contract_record")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractRecord {
    @TableId(value = "record_id", type = IdType.AUTO)
    private Integer id;

    @TableField("record_contract_id")
    private Integer contractId;

    @TableField("record_operator_id")
    private Integer operatorId;

    @TableField("record_operation_type")
    private Integer type;

    @TableField("record_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    @TableField("record_details")
    private String details;
}
