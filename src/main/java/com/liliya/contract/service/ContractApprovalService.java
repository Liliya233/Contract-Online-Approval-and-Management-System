package com.liliya.contract.service;

import com.liliya.contract.model.domain.Contract;
import com.liliya.contract.model.exception.ContractException;

public interface ContractApprovalService {
    // 提交审批
    Contract ApprovalSubmit(Integer id) throws ContractException;

    // 撤回审批
    Contract ApprovalCancel(Integer id) throws ContractException;

    // 同意审批
    Contract ApprovalAllow(Integer id, String note) throws ContractException;

    // 拒绝审批
    Contract ApprovalReject(Integer id) throws ContractException;

    // 盖章
    Contract Seal(Integer id, String note) throws ContractException;

    // 确认上传扫描件
    Contract ScanSubmit(Integer id) throws ContractException;

    // 同意备案
    Contract RecordAllow(Integer id, String note) throws ContractException;

    // 拒绝备案
    Contract RecordReject(Integer id) throws ContractException;

    // 提交终结
    Contract EndSubmit(Integer id, String note) throws ContractException;

    // 同意备案
    Contract EndAllow(Integer id, String note) throws ContractException;

    // 拒绝备案
    Contract EndReject(Integer id) throws ContractException;
}
