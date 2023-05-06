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
import java.util.HashMap;
import java.util.Map;

@TableName("supplement")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Supplement {
    @TableId(value = "supplement_id", type = IdType.AUTO)
    private Integer id;

    @TableField("supplement_contract_id")
    private Integer contractId;

    @TableField("supplement_name")
    private String name;

    @TableField("supplement_create_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField("supplement_status")
    private Integer status;

    @TableField("supplement_seal_author_id")
    private Integer sealAuthorId;

    @TableField("supplement_seal_note")
    private String sealNote;

    // 协议状态
    public static final Map<String, Integer> STATUS = new HashMap<>(){
        {
            put("APPROVAL_AWAIT", 0);
            put("APPROVAL_DEPARTMENT", 1);
            put("APPROVAL_COMPANY", 2);
            put("APPROVAL_FINAL", 3);
            put("APPROVAL_FAIL", 4);
            put("SEAL_AWAIT", 5);
            put("SCAN_AWAIT", 6);
            put("RECORD_AWAIT", 7);
            put("RECORD_FAIL", 8);
            put("RECORD_PASS", 9);
            put("END_APPROVAL_DEPARTMENT", 10);
            put("END_APPROVAL_COMPANY", 11);
            put("END_APPROVAL_FINAL", 12);
            put("END_APPROVAL_FAIL", 13);
            put("ENDED", 14);
        }
    };
}
