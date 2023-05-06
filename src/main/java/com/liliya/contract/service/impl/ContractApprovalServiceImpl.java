package com.liliya.contract.service.impl;

import com.liliya.contract.dao.ContractMapper;
import com.liliya.contract.dao.SupplementMapper;
import com.liliya.contract.model.define.FileType;
import com.liliya.contract.model.define.OperationType;
import com.liliya.contract.model.define.Status;
import com.liliya.contract.model.domain.*;
import com.liliya.contract.model.exception.ContractException;
import com.liliya.contract.model.exception.SupplementException;
import com.liliya.contract.service.ContractApprovalService;
import com.liliya.contract.utils.PDFUtils;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class ContractApprovalServiceImpl implements ContractApprovalService {
    @Resource
    private ContractMapper contractMapper;

    @Resource
    private DepartmentServiceImpl departmentService;

    @Resource
    private UserServiceImpl userService;

    @Resource
    private ContractServiceImpl contractService;

    @Resource
    private ContractRecordServiceImpl contractRecordService;

    @Resource
    private ContractAttachmentServiceImpl contractAttachmentService;

    @Resource
    private SupplementRecordServiceImpl supplementRecordService;

    @Resource
    private SupplementServiceImpl supplementService;

    @Resource
    private SupplementMapper supplementMapper;

    @Resource
    private PDFUtils pdfUtils;

    @Override
    @CachePut(cacheNames = "contract-id", key = "#id")
    public Contract ApprovalSubmit(Integer id) throws ContractException {
        // 获取当前合同
        Contract contract = contractService.getById(id);
        // 合法性检查
        if (contract == null || !contractService.hasAccessPermission(contract)){
            throw ContractException.PermissionDeny();
        }
        // 完整性检查
        if (contract.getType() == null){
            throw ContractException.NotFinish();
        }
        if (contractAttachmentService.getByContractId(contract.getId(), FileType.FILE).isEmpty()){
            throw ContractException.NotFile();
        }
        // 合同状态检查
        Integer status = contract.getStatus();
        if (!status.equals(Status.APPROVAL_AWAIT) && !status.equals(Status.APPROVAL_FAIL)){
            throw ContractException.ApprovalFail();
        }
        // 更新合同状态
        contract.setStatus(Status.APPROVAL_DEPARTMENT);
        if (contractMapper.updateById(contract) < 1){
            throw ContractException.EditFail();
        }
        // 新增记录
        ContractRecord contractRecord = ContractRecord
                .builder()
                .type(OperationType.APPROVAL_SUBMIT)
                .details("提交合同审批")
                .build();
        contractRecordService.add(contract, contractRecord);
        return contract;
    }

    @Override
    @CachePut(cacheNames = "contract-id", key = "#id")
    public Contract ApprovalCancel(Integer id) throws ContractException {
        // 获取当前合同
        Contract contract = contractService.getById(id);
        // 合法性检查
        if (contract == null || !contractService.hasAccessPermission(contract)){
            throw ContractException.PermissionDeny();
        }
        // 合同状态检查
        Integer status = contract.getStatus();
        if (!status.equals(Status.APPROVAL_DEPARTMENT) && !status.equals(Status.APPROVAL_COMPANY)
                && !status.equals(Status.APPROVAL_FINAL)){
            throw ContractException.ApprovalCancelFail();
        }
        // 更新合同状态
        contract.setStatus(Status.APPROVAL_AWAIT);
        if (contractMapper.updateById(contract) < 1){
            throw ContractException.EditFail();
        }
        // 新增记录
        ContractRecord contractRecord = ContractRecord
                .builder()
                .type(OperationType.APPROVAL_DROP)
                .details("撤回合同审批")
                .build();
        contractRecordService.add(contract, contractRecord);
        return contract;
    }

    @Override
    @CachePut(cacheNames = "contract-id", key = "#id")
    public Contract ApprovalAllow(Integer id, String note) throws ContractException {
        // 获取当前合同
        Contract contract = contractService.getById(id);
        // 合法性检查
        if (contract == null || !contractService.hasApprovalPermission(contract)){
            throw ContractException.PermissionDeny();
        }
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            throw ContractException.ApprovalFail();
        }
        // 获取当前用户部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 操作类型
        int operationType;
        // 根据部门状态更新合同
        switch (department.getPermissionApproval()){
            case Status.APPROVAL_DEPARTMENT:
                contract.setStatus(Status.APPROVAL_COMPANY);
                operationType = OperationType.APPROVAL_DEPARTMENT_ALLOW;
                break;
            case Status.APPROVAL_COMPANY:
                contract.setStatus(Status.APPROVAL_FINAL);
                operationType = OperationType.APPROVAL_COMPANY_ALLOW;
                break;
            case Status.APPROVAL_FINAL:
                // 最终审批通过，尝试为合同文件增加水印
                List<ContractAttachment> attachments = contractAttachmentService.getByContractId(contract.getId(), FileType.FILE);
                if (attachments.isEmpty()){
                    throw ContractException.ApprovalAllowFail();
                }
                if (!pdfUtils.addWaterMark(attachments.get(0).getUrl())){
                    // 出现问题时不更新合同信息
                    throw ContractException.ApprovalAllowFail();
                }
                contract.setStatus(Status.SEAL_AWAIT);
                operationType = OperationType.APPROVAL_FINAL_ALLOW;
                break;
            default:
                throw ContractException.PermissionDeny();
        }
        // 更新合同状态
        if (contractMapper.updateById(contract) < 1){
            throw ContractException.EditFail();
        }
        // 新增记录
        ContractRecord contractRecord = ContractRecord
                .builder()
                .type(operationType)
                .details("同意合同审批，备注：" + (note.isEmpty() ? "无" : note))
                .build();
        contractRecordService.add(contract, contractRecord);
        return contract;
    }

    @Override
    @CachePut(cacheNames = "contract-id", key = "#id")
    public Contract ApprovalReject(Integer id) throws ContractException {
        // 获取当前合同
        Contract contract = contractService.getById(id);
        // 合法性检查
        if (contract == null || !contractService.hasApprovalPermission(contract)){
            throw ContractException.PermissionDeny();
        }
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            throw ContractException.ApprovalFail();
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
                throw ContractException.PermissionDeny();
        }
        // 更新合同状态
        contract.setStatus(Status.APPROVAL_FAIL);
        if (contractMapper.updateById(contract) < 1){
            throw ContractException.EditFail();
        }
        // 新增记录
        ContractRecord contractRecord = ContractRecord
                .builder()
                .type(operationType)
                .details("不同意合同审批")
                .build();
        contractRecordService.add(contract, contractRecord);
        return contract;
    }

    @Override
    @CachePut(cacheNames = "contract-id", key = "#id")
    public Contract Seal(Integer id, String note) throws ContractException {
        // 获取当前合同
        Contract contract = contractService.getById(id);
        // 合同合法性检查
        if (contract == null || !contract.getStatus().equals(Status.SEAL_AWAIT)){
            throw ContractException.PermissionDeny();
        }
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            throw ContractException.ApprovalFail();
        }
        // 获取当前用户部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 权限合法性检查
        if (!department.isPermissionSeal()){
            throw ContractException.PermissionDeny();
        }
        // 尝试为合同文件盖章
        List<ContractAttachment> attachments = contractAttachmentService.getByContractId(contract.getId(), FileType.FILE);
        if (attachments.isEmpty()){
            throw ContractException.SealFail();
        }
        if (!pdfUtils.addSeal(attachments.get(0).getUrl())){
            // 出现问题时不更新合同信息
            throw ContractException.SealFail();
        }
        // 写入盖章信息
        String sealNote = note.isEmpty() ? "无" : note;
        contract.setSealNote(sealNote);
        contract.setSealAuthorId(user.getId());
        // 更新合同状态
        contract.setStatus(Status.SCAN_AWAIT);
        if (contractMapper.updateById(contract) < 1){
            throw ContractException.EditFail();
        }
        // 新增记录
        ContractRecord contractRecord = ContractRecord
                .builder()
                .type(OperationType.SEAL)
                .details("合同盖章，备注：" + sealNote)
                .build();
        contractRecordService.add(contract, contractRecord);
        return contract;
    }

    @Override
    @CachePut(cacheNames = "contract-id", key = "#id")
    public Contract ScanSubmit(Integer id) throws ContractException {
        // 获取当前合同
        Contract contract = contractService.getById(id);
        // 合同合法性检查
        if (!contractService.isSelf(contract)){
            throw ContractException.PermissionDeny();
        }
        // 合同流程检查
        Integer status = contract.getStatus();
        if (!status.equals(Status.SCAN_AWAIT) && !status.equals(Status.RECORD_FAIL)){
            throw ContractException.PermissionDeny();
        }
        // 检查是否已上传文件
        if (contractAttachmentService.getByContractId(contract.getId(), FileType.SCAN) == null){
            throw ContractException.NotScan();
        }
        // 更新合同状态
        contract.setStatus(Status.RECORD_AWAIT);
        if (contractMapper.updateById(contract) < 1){
            throw ContractException.EditFail();
        }
        // 新增操作记录
        ContractRecord contractRecord = ContractRecord
                .builder()
                .type(OperationType.SCAN)
                .details("提交合同扫描版")
                .build();
        contractRecordService.add(contract, contractRecord);
        return contract;
    }

    @Override
    @CachePut(cacheNames = "contract-id", key = "#id")
    public Contract RecordAllow(Integer id, String note) throws ContractException {
        // 获取当前合同
        Contract contract = contractService.getById(id);
        // 合同合法性检查
        if (contract == null || !contract.getStatus().equals(Status.RECORD_AWAIT)){
            throw ContractException.PermissionDeny();
        }
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            throw ContractException.EditFail();
        }
        // 获取当前用户部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 权限合法性检查
        if (!department.isPermissionSeal()){
            throw ContractException.PermissionDeny();
        }
        // 更新合同状态
        contract.setStatus(Status.RECORD_PASS);
        if (contractMapper.updateById(contract) < 1){
            throw ContractException.EditFail();
        }
        // 新增操作记录
        ContractRecord contractRecord = ContractRecord
                .builder()
                .type(OperationType.RECORD_ALLOW)
                .details("合同备案通过，备注：" + (note.isEmpty() ? "无" : note))
                .build();
        contractRecordService.add(contract, contractRecord);
        return contract;
    }

    @Override
    @CachePut(cacheNames = "contract-id", key = "#id")
    public Contract RecordReject(Integer id) throws ContractException {
        // 获取当前合同
        Contract contract = contractService.getById(id);
        // 合同合法性检查
        if (contract == null || !contract.getStatus().equals(Status.RECORD_AWAIT)){
            throw ContractException.PermissionDeny();
        }
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            throw ContractException.EditFail();
        }
        // 获取当前用户部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 权限合法性检查
        if (!department.isPermissionSeal()){
            throw ContractException.PermissionDeny();
        }
        // 更新合同状态
        contract.setStatus(Status.RECORD_FAIL);
        if (contractMapper.updateById(contract) < 1){
            throw ContractException.EditFail();
        }
        // 新增操作记录
        ContractRecord contractRecord = ContractRecord
                .builder()
                .type(OperationType.RECORD_FAIL)
                .details("合同备案不通过")
                .build();
        contractRecordService.add(contract, contractRecord);
        return contract;
    }

    @Override
    @CachePut(cacheNames = "contract-id", key = "#id")
    public Contract EndSubmit(Integer id, String note) throws ContractException {
        // 获取当前合同
        Contract contract = contractService.getById(id);
        // 合同合法性检查
        if (!contractService.isSelf(contract)){
            throw ContractException.PermissionDeny();
        }
        // 合同流程检查
        Integer status = contract.getStatus();
        if (!status.equals(Status.RECORD_PASS) && !status.equals(Status.END_FAIL)){
            throw ContractException.PermissionDeny();
        }
        // 检查是否完成
        if (note == null || note.isEmpty()){
            throw ContractException.NotFinish();
        }
        // 更新合同状态
        contract.setEndNote(note);
        contract.setStatus(Status.END_AWAIT);
        if (contractMapper.updateById(contract) < 1){
            throw ContractException.EditFail();
        }
        // 新增操作记录
        ContractRecord contractRecord = ContractRecord
                .builder()
                .type(OperationType.END_SUBMIT)
                .details("提交合同终结审批")
                .build();
        contractRecordService.add(contract, contractRecord);
        return contract;
    }

    @Override
    @CachePut(cacheNames = "contract-id", key = "#id")
    public Contract EndAllow(Integer id, String note) throws ContractException {
        // 获取当前合同
        Contract contract = contractService.getById(id);
        // 合同合法性检查
        if (contract == null || !contract.getStatus().equals(Status.END_AWAIT)){
            throw ContractException.PermissionDeny();
        }
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            throw ContractException.EditFail();
        }
        // 获取当前用户部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 权限合法性检查
        if (!department.isPermissionTerminate()){
            throw ContractException.PermissionDeny();
        }
        // 更新合同状态
        contract.setStatus(Status.ENDED);
        if (contractMapper.updateById(contract) < 1){
            throw ContractException.EditFail();
        }
        // 新增操作记录
        ContractRecord contractRecord = ContractRecord
                .builder()
                .type(OperationType.END_ALLOW)
                .details("合同终结审批通过，备注：" + (note.isEmpty() ? "无" : note))
                .build();
        contractRecordService.add(contract, contractRecord);
        // 更新对应的协议
        supplementService.getByContractId(contract.getId()).forEach(supplement -> {
            // 删除未通过的协议
            if (!supplement.getStatus().equals(Status.RECORD_PASS)){
                supplementMapper.deleteById(supplement);
                return;
            }
            // 更新状态
            supplement.setStatus(Status.ENDED);
            try {
                supplementService.update(supplement);
                // 写入记录
                supplementRecordService.add(supplement, SupplementRecord.builder()
                        .type(OperationType.END_ALLOW)
                        .details("合同终结审批通过，备注：" + (note.isEmpty() ? "无" : note))
                        .build()
                );
            } catch (SupplementException ignored) {}
        });
        return contract;
    }

    @Override
    @CachePut(cacheNames = "contract-id", key = "#id")
    public Contract EndReject(Integer id) throws ContractException {
        // 获取当前合同
        Contract contract = contractService.getById(id);
        // 合同合法性检查
        if (contract == null || !contract.getStatus().equals(Status.END_AWAIT)){
            throw ContractException.PermissionDeny();
        }
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            throw ContractException.EditFail();
        }
        // 获取当前用户部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 权限合法性检查
        if (!department.isPermissionTerminate()){
            throw ContractException.PermissionDeny();
        }
        // 更新合同状态
        contract.setStatus(Status.END_FAIL);
        if (contractMapper.updateById(contract) < 1){
            throw ContractException.EditFail();
        }
        // 新增操作记录
        ContractRecord contractRecord = ContractRecord
                .builder()
                .type(OperationType.END_REJECT)
                .details("合同终结审批不通过")
                .build();
        contractRecordService.add(contract, contractRecord);
        return contract;
    }
}
