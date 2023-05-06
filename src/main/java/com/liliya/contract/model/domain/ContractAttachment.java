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

@TableName("contract_attachment")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractAttachment {
    @TableId(value = "attachment_id", type = IdType.AUTO)
    private Integer id;

    @TableField("attachment_contract_id")
    private Integer contractId;

    @TableField("attachment_type")
    private Integer type;

    @TableField("attachment_name")
    private String name;

    @TableField("attachment_size")
    private Long size;

    @TableField("attachment_upload_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;

    @TableField("attachment_url")
    private String url;
}
