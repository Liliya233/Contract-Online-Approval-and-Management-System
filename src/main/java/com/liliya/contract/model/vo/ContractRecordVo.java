package com.liliya.contract.model.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class ContractRecordVo {
    @TableId
    private Integer recordId;

    private Integer recordOperationType;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date recordTime;

    private Integer recordContractId;

    private Integer recordOperatorId;

    private Integer userId;

    private Integer userDepartmentId;

    private Long contractFunds;
}
