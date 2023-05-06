package com.liliya.contract.service.impl;

import com.liliya.contract.dao.SupplementMapper;
import com.liliya.contract.model.define.FileType;
import com.liliya.contract.model.define.OperationType;
import com.liliya.contract.model.define.Status;
import com.liliya.contract.model.domain.*;
import com.liliya.contract.model.exception.SupplementException;
import com.liliya.contract.service.SupplementApprovalService;
import com.liliya.contract.utils.PDFUtils;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class SupplementApprovalServiceImpl implements SupplementApprovalService {
    @Resource
    private SupplementMapper supplementMapper;

    @Resource
    private DepartmentServiceImpl departmentService;

    @Resource
    private UserServiceImpl userService;

    @Resource
    private SupplementServiceImpl supplementService;

    @Resource
    private SupplementRecordServiceImpl supplementRecordService;

    @Resource
    private SupplementAttachmentServiceImpl supplementAttachmentService;

    @Resource
    private ContractRecordServiceImpl contractRecordService;

    @Resource
    private ContractServiceImpl contractService;

    @Resource
    private PDFUtils pdfUtils;

    @Override
    @CachePut(cacheNames = "supplement-id", key = "#id")
    public Supplement ApprovalSubmit(Integer id) throws SupplementException {
        // 获取当前协议
        Supplement supplement = supplementService.getById(id);
        // 合法性检查
        if (!supplementService.hasAccessPermission(supplement)){
            throw SupplementException.PermissionDeny();
        }
        // 完整性检查
        if (supplement.getName() == null){
            throw SupplementException.NotFinish();
        }
        if (supplementAttachmentService.getBySupplementId(supplement.getId(), FileType.FILE).isEmpty()){
            throw SupplementException.NotFile();
        }
        // 协议状态检查
        Integer status = supplement.getStatus();
        if (!status.equals(Status.APPROVAL_AWAIT) && !status.equals(Status.APPROVAL_FAIL)){
            throw SupplementException.ApprovalFail();
        }
        // 更新协议状态
        supplement.setStatus(Status.APPROVAL_DEPARTMENT);
        if (supplementMapper.updateById(supplement) < 1){
            throw SupplementException.EditFail();
        }
        // 新增记录
        SupplementRecord supplementRecord = SupplementRecord
                .builder()
                .type(OperationType.APPROVAL_SUBMIT)
                .details("提交协议审批")
                .build();
        supplementRecordService.add(supplement, supplementRecord);
        return supplement;
    }

    @Override
    @CachePut(cacheNames = "supplement-id", key = "#id")
    public Supplement ApprovalCancel(Integer id) throws SupplementException {
        // 获取当前协议
        Supplement supplement = supplementService.getById(id);
        // 合法性检查
        if (!supplementService.hasAccessPermission(supplement)){
            throw SupplementException.PermissionDeny();
        }
        // 协议状态检查
        Integer status = supplement.getStatus();
        if (!status.equals(Status.APPROVAL_DEPARTMENT) && !status.equals(Status.APPROVAL_COMPANY)
                && !status.equals(Status.APPROVAL_FINAL)){
            throw SupplementException.ApprovalCancelFail();
        }
        // 更新协议状态
        supplement.setStatus(Status.APPROVAL_AWAIT);
        if (supplementMapper.updateById(supplement) < 1){
            throw SupplementException.EditFail();
        }
        // 新增记录
        SupplementRecord supplementRecord = SupplementRecord
                .builder()
                .type(OperationType.APPROVAL_DROP)
                .details("撤回协议审批")
                .build();
        supplementRecordService.add(supplement, supplementRecord);
        return supplement;
    }

    @Override
    @CachePut(cacheNames = "supplement-id", key = "#id")
    public Supplement ApprovalAllow(Integer id, String note) throws SupplementException {
        // 获取当前协议
        Supplement supplement = supplementService.getById(id);
        // 合法性检查
        if (!supplementService.hasApprovalPermission(supplement)){
            throw SupplementException.PermissionDeny();
        }
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            throw SupplementException.PermissionDeny();
        }
        // 获取当前用户部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 操作类型
        int operationType;
        // 根据部门状态更新协议
        switch (department.getPermissionApproval()){
            case Status.APPROVAL_DEPARTMENT:
                supplement.setStatus(Status.APPROVAL_COMPANY);
                operationType = OperationType.APPROVAL_DEPARTMENT_ALLOW;
                break;
            case Status.APPROVAL_COMPANY:
                supplement.setStatus(Status.APPROVAL_FINAL);
                operationType = OperationType.APPROVAL_COMPANY_ALLOW;
                break;
            case Status.APPROVAL_FINAL:
                // 最终审批通过，尝试为协议文件增加水印
                List<SupplementAttachment> attachments = supplementAttachmentService.getBySupplementId(supplement.getId(), FileType.FILE);
                if (attachments.isEmpty()){
                    throw SupplementException.ApprovalAllowFail();
                }
                if (!pdfUtils.addWaterMark(attachments.get(0).getUrl())){
                    // 出现问题时不更新协议信息
                    throw SupplementException.ApprovalAllowFail();
                }
                supplement.setStatus(Status.SEAL_AWAIT);
                operationType = OperationType.APPROVAL_FINAL_ALLOW;
                break;
            default:
                throw SupplementException.PermissionDeny();
        }
        // 更新协议状态
        if (supplementMapper.updateById(supplement) < 1){
            throw SupplementException.EditFail();
        }
        // 新增记录
        SupplementRecord supplementRecord = SupplementRecord
                .builder()
                .type(operationType)
                .details("同意协议审批，备注：" + (note.isEmpty() ? "无" : note))
                .build();
        supplementRecordService.add(supplement, supplementRecord);
        return supplement;
    }

    @Override
    @CachePut(cacheNames = "supplement-id", key = "#id")
    public Supplement ApprovalReject(Integer id) throws SupplementException {
        // 获取当前协议
        Supplement supplement = supplementService.getById(id);
        // 合法性检查
        if (!supplementService.hasApprovalPermission(supplement)){
            throw SupplementException.PermissionDeny();
        }
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            throw SupplementException.PermissionDeny();
        }
        // 获取当前用户部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 操作类型获取
        int operationType;
        switch (department.getPermissionApproval()){
            case Status.APPROVAL_DEPARTMENT:
                operationType = OperationType.APPROVAL_DEPARTMENT_REJECT;
                break;
            case Status.APPROVAL_COMPANY:
                operationType = OperationType.APPROVAL_COMPANY_REJECT;
                break;
            case Status.APPROVAL_FINAL:
                operationType = OperationType.APPROVAL_FINAL_REJECT;
                break;
            default:
                throw SupplementException.PermissionDeny();
        }
        // 更新协议状态
        supplement.setStatus(Status.APPROVAL_FAIL);
        if (supplementMapper.updateById(supplement) < 1){
            throw SupplementException.EditFail();
        }
        // 新增记录
        SupplementRecord supplementRecord = SupplementRecord
                .builder()
                .type(operationType)
                .details("不同意协议审批")
                .build();
        supplementRecordService.add(supplement, supplementRecord);
        return supplement;
    }

    @Override
    @CachePut(cacheNames = "supplement-id", key = "#id")
    public Supplement Seal(Integer id, String note) throws SupplementException {
        // 获取当前协议
        Supplement supplement = supplementService.getById(id);
        // 协议合法性检查
        if (supplement == null || !supplement.getStatus().equals(Status.SEAL_AWAIT)){
            throw SupplementException.PermissionDeny();
        }
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            throw SupplementException.PermissionDeny();
        }
        // 获取当前用户部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 权限合法性检查
        if (!department.isPermissionSeal()){
            throw SupplementException.PermissionDeny();
        }
        // 尝试为协议文件盖章
        List<SupplementAttachment> attachments = supplementAttachmentService.getBySupplementId(supplement.getId(), FileType.FILE);
        if (attachments.isEmpty()){
            throw SupplementException.SealFail();
        }
        if (!pdfUtils.addSeal(attachments.get(0).getUrl())){
            // 出现问题时不更新协议信息
            throw SupplementException.SealFail();
        }
        // 写入盖章信息
        String sealNote = note.isEmpty() ? "无" : note;
        supplement.setSealNote(sealNote);
        supplement.setSealAuthorId(user.getId());
        // 更新协议状态
        supplement.setStatus(Status.SCAN_AWAIT);
        if (supplementMapper.updateById(supplement) < 1){
            throw SupplementException.EditFail();
        }
        // 新增记录
        SupplementRecord supplementRecord = SupplementRecord
                .builder()
                .type(OperationType.SEAL)
                .details("协议盖章，备注：" + sealNote)
                .build();
        supplementRecordService.add(supplement, supplementRecord);
        return supplement;
    }

    @Override
    @CachePut(cacheNames = "supplement-id", key = "#id")
    public Supplement ScanSubmit(Integer id) throws SupplementException {
        // 获取当前协议
        Supplement supplement = supplementService.getById(id);
        // 协议合法性检查
        if (!supplementService.isSelf(supplement)){
            throw SupplementException.PermissionDeny();
        }
        // 协议流程检查
        Integer status = supplement.getStatus();
        if (!status.equals(Status.SCAN_AWAIT) && !status.equals(Status.RECORD_FAIL)){
            throw SupplementException.PermissionDeny();
        }
        // 检查是否已上传文件
        if (supplementAttachmentService.getBySupplementId(supplement.getId(), FileType.SCAN) == null){
            throw SupplementException.NotScan();
        }
        // 更新协议状态
        supplement.setStatus(Status.RECORD_AWAIT);
        if (supplementMapper.updateById(supplement) < 1){
            throw SupplementException.EditFail();
        }
        // 新增操作记录
        SupplementRecord supplementRecord = SupplementRecord
                .builder()
                .type(OperationType.SCAN)
                .details("提交协议扫描版")
                .build();
        supplementRecordService.add(supplement, supplementRecord);
        return supplement;
    }

    @Override
    @CachePut(cacheNames = "supplement-id", key = "#id")
    public Supplement RecordAllow(Integer id, String note) throws SupplementException {
        // 获取当前协议
        Supplement supplement = supplementService.getById(id);
        // 协议合法性检查
        if (supplement == null || !supplement.getStatus().equals(Status.RECORD_AWAIT)){
            throw SupplementException.PermissionDeny();
        }
        // 获取当前协议合同
        Contract contract = contractService.getById(supplement.getContractId());
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            throw SupplementException.PermissionDeny();
        }
        // 获取当前用户部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 权限合法性检查
        if (!department.isPermissionSeal()){
            throw SupplementException.PermissionDeny();
        }
        // 更新协议状态
        supplement.setStatus(Status.RECORD_PASS);
        if (supplementMapper.updateById(supplement) < 1){
            throw SupplementException.EditFail();
        }
        // 新增操作记录
        SupplementRecord supplementRecord = SupplementRecord
                .builder()
                .type(OperationType.RECORD_ALLOW)
                .details("协议备案通过，备注：" + (note.isEmpty() ? "无" : note))
                .build();
        supplementRecordService.add(supplement, supplementRecord);
        // 新增合同操作记录
        ContractRecord contractRecord = ContractRecord
                .builder()
                .type(OperationType.CREATE_SUPPLEMENT)
                .details("新增补充协议，名称：" + supplement.getName())
                .build();
        contractRecordService.add(contract, contractRecord);
        return supplement;
    }

    @Override
    @CachePut(cacheNames = "supplement-id", key = "#id")
    public Supplement RecordReject(Integer id) throws SupplementException {
        // 获取当前协议
        Supplement supplement = supplementService.getById(id);
        // 协议合法性检查
        if (supplement == null || !supplement.getStatus().equals(Status.RECORD_AWAIT)){
            throw SupplementException.PermissionDeny();
        }
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            throw SupplementException.PermissionDeny();
        }
        // 获取当前用户部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 权限合法性检查
        if (!department.isPermissionSeal()){
            throw SupplementException.PermissionDeny();
        }
        // 更新协议状态
        supplement.setStatus(Status.RECORD_FAIL);
        if (supplementMapper.updateById(supplement) < 1){
            throw SupplementException.EditFail();
        }
        // 新增操作记录
        SupplementRecord supplementRecord = SupplementRecord
                .builder()
                .type(OperationType.RECORD_FAIL)
                .details("协议备案不通过")
                .build();
        supplementRecordService.add(supplement, supplementRecord);
        return supplement;
    }
}
