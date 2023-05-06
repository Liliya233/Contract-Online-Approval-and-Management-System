package com.liliya.contract.service;

import com.liliya.contract.model.domain.Supplement;
import com.liliya.contract.model.exception.SupplementException;

public interface SupplementApprovalService {
    // 提交审批
    Supplement ApprovalSubmit(Integer id) throws SupplementException;

    // 撤回审批
    Supplement ApprovalCancel(Integer id) throws SupplementException;

    // 同意审批
    Supplement ApprovalAllow(Integer id, String note) throws SupplementException;

    // 拒绝审批
    Supplement ApprovalReject(Integer id) throws SupplementException;

    // 盖章
    Supplement Seal(Integer id, String note) throws SupplementException;

    // 确认上传扫描件
    Supplement ScanSubmit(Integer id) throws SupplementException;

    // 同意备案
    Supplement RecordAllow(Integer id, String note) throws SupplementException;

    // 拒绝备案
    Supplement RecordReject(Integer id) throws SupplementException;
}
