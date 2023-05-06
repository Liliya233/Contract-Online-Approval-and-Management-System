package com.liliya.contract.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.liliya.contract.dao.ContractMapper;
import com.liliya.contract.model.define.FileType;
import com.liliya.contract.model.define.OperationType;
import com.liliya.contract.model.define.Status;
import com.liliya.contract.model.domain.*;
import com.liliya.contract.model.exception.ContractException;
import com.liliya.contract.model.response.LayuiFormDataResponse;
import com.liliya.contract.service.ContractService;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class ContractServiceImpl implements ContractService {
    @Resource
    private ContractMapper contractMapper;

    @Resource
    private DepartmentServiceImpl departmentService;

    @Resource
    private UserServiceImpl userService;

    @Resource
    private ContractRecordServiceImpl contractRecordService;

    @Resource
    private ContractAttachmentServiceImpl contractAttachmentService;

    @Resource
    private SupplementServiceImpl supplementService;

    @Resource
    private ContractServiceImpl contractService;

    @Override
    @CachePut(cacheNames = "contract-id", key = "#result.getId()")
    public Contract add() throws ContractException {
        User user = userService.getAuthentication();
        if (user == null){
            throw ContractException.AddFail();
        }
        // 新建合同对象
        Contract contract = Contract
                .builder()
                .status(Status.APPROVAL_AWAIT)
                .authorId(user.getId())
                .createTime(new Date(System.currentTimeMillis()))
                .build();
        // 插入
        if(contractMapper.insert(contract) < 1){
            throw ContractException.AddFail();
        }
        // 新增记录
        ContractRecord contractRecord = ContractRecord
                .builder()
                .type(OperationType.CREATE)
                .details("新增合同")
                .build();
        contractRecordService.add(contract, contractRecord);
        return contract;
    }

    @Override
    @CacheEvict(cacheNames = "contract-id", key = "#id")
    public Contract delete(Integer id) throws ContractException {
        // 获取当前合同
        Contract contract = ((ContractServiceImpl) AopContext.currentProxy()).getById(id);
        // 合法性检查
        if (contract == null || !((ContractServiceImpl) AopContext.currentProxy()).hasAccessPermission(contract)){
            throw ContractException.PermissionDeny();
        }
        // 不允许删除通过备案的合同
        if(contract.getStatus() >= Status.RECORD_PASS){
            throw ContractException.DeleteFail();
        }
        // 删除
        if (contractMapper.deleteById(id) < 1){
            throw ContractException.DeleteFail();
        }
        return contract;
    }

    @Override
    @CachePut(cacheNames = "contract-id", key = "#id")
    public Contract update(Integer id, String name, Integer type, Date startDate, Date endDate, Date periodStartDate,
                           Date periodExpireDate, String party, Long funds) throws ContractException {
        // 获取当前合同
        Contract contract = ((ContractServiceImpl) AopContext.currentProxy()).getById(id);
        // 合法性检查
        if (contract == null || !((ContractServiceImpl) AopContext.currentProxy()).isSelf(contract)){
            throw ContractException.PermissionDeny();
        }
        // 仅允许修改未审核与审核不通过的合同
        Integer status = contract.getStatus();
        if (!status.equals(Status.APPROVAL_AWAIT) && !status.equals(Status.APPROVAL_FAIL)){
            throw ContractException.EditFail();
        }
        // 修改对象
        contract.setName(name);
        contract.setType(type);
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setPeriodStartDate(periodStartDate);
        contract.setPeriodExpireDate(periodExpireDate);
        contract.setParty(party);
        contract.setFunds(funds);
        // 插入
        if (contractMapper.updateById(contract) < 1){
            throw ContractException.EditFail();
        }
        return contract;
    }

    @Override
    @Cacheable(cacheNames = "contract-id", key = "#id", unless = "#result == null")
    public Contract getById(Integer id){
        return contractMapper.selectById(id);
    }

    @Override
    public LayuiFormDataResponse<Object> getPage(Integer pageType, Integer page, Integer limit, Integer type,
                                                 Integer status, String name, String author, String party) {
        List<Map<String, Object>> result = new ArrayList<>();
        QueryWrapper<Contract> wrapper = new QueryWrapper<>();
        // 排序
        wrapper.orderByAsc("contract_id");
        // 将作者 ID 放入 wrapper
        if (author != null && !author.isEmpty()){
            User authorUser = userService.getByName(author);
            if (authorUser != null){
                wrapper.eq("contract_author_id", authorUser.getId());
            }else {
                return LayuiFormDataResponse.ok("成功返回", 0L, result);
            }
        }
        // 将搜索条件放入 wrapper
        wrapper.eq(type != null, "contract_type", type)
                .eq(status != null, "contract_status", status)
                .like(name != null && !name.isEmpty(), "contract_name", name)
                .like(party != null && !party.isEmpty(), "contract_party", party);
        // 根据请求状态进行合法性检查
        User user = userService.getAuthentication();
        if (user == null){
            return LayuiFormDataResponse.ok("成功返回", 0L, result);
        }
        Department department = departmentService.getById(user.getDepartmentId());
        switch (pageType){
            // 仅获取有访问权限的合同
            case 0:
                switch (department.getPermissionAccess()){
                    // 个人
                    case 0:
                        wrapper.eq("user_id", user.getId());
                        break;
                    // 部门
                    case 1:
                        wrapper.eq("user_department_id", user.getDepartmentId());
                        break;
                    // 公司
                    case 2:
                        break;
                }
                break;
            // 仅获取有审批权限的合同
            case 1:
                switch (department.getPermissionApproval()){
                    // 禁止
                    case 0:
                        return LayuiFormDataResponse.ok("成功返回", 0L, new ArrayList<>());
                    // 部门
                    case 1:
                        wrapper.eq("user_department_id", user.getDepartmentId());
                        wrapper.eq("contract_status", Status.APPROVAL_DEPARTMENT);
                        break;
                    // 公司
                    case 2:
                        wrapper.between("contract_status", Status.APPROVAL_DEPARTMENT, Status.APPROVAL_COMPANY);
                        break;
                    // 最终
                    case 3:
                        wrapper.between("contract_status", Status.APPROVAL_DEPARTMENT, Status.APPROVAL_FINAL);
                        break;
                }
                break;
            // 仅获取等待盖章的合同
            case 2:
                wrapper.eq("contract_status", Status.SEAL_AWAIT);
                break;
            // 仅获取等待备案的合同
            case 3:
                wrapper.eq("contract_status", Status.RECORD_AWAIT);
                break;
            // 仅获取等待终结的合同
            case 4:
                wrapper.eq("contract_status", Status.END_AWAIT);
                break;
        }
        // 合同信息获取
        Page<Contract> newPage = new Page<>(page, limit);
        newPage.setOptimizeJoinOfCountSql(false);
        IPage<Contract> queryPage = contractMapper.findPageWithUserInfo(newPage, wrapper);
        // 组装合同信息
        queryPage.getRecords().forEach(contract -> {
            // 组装数据
            Integer authorId = contract.getAuthorId();
            // 获取作者用户对象
            User authorUser = null;
            if (authorId != null){
                authorUser = userService.getById(authorId);
            }
            result.add(Map.of(
                    "id", contract.getId(),
                    "name", contract.getName() != null ? contract.getName() : "",
                    "type", contract.getType() != null ? contract.getType() : "",
                    "author_name", authorUser != null ? authorUser.getName() : "",
                    "party", contract.getParty() != null ? contract.getParty() : "",
                    "status", contract.getStatus(),
                    "self", contractService.isSelf(contract)
            ));
        });
        return LayuiFormDataResponse.ok("成功返回", queryPage.getTotal(), result);
    }

    @Override
    public Boolean hasAccessPermission(Contract contract) {
        if (contract == null){
            return false;
        }
        // 获取合同作者
        User author = userService.getById(contract.getAuthorId());
        if (author == null){
            return false;
        }
        // 获取作者部门
        Department authorDepartment = departmentService.getById(author.getDepartmentId());
        if (authorDepartment == null){
            return false;
        }
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            return false;
        }
        // 获取当前用户部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 根据用户部门数据访问权限决定处理方式
        switch (department.getPermissionAccess()){
            // 个人 (仅允许访问个人合同)
            case 0:
                if (!author.getId().equals(user.getId())){
                    return false;
                }
                break;
            // 部门 (仅允许访问当前部门合同与个人合同)
            case 1:
                if (!authorDepartment.getId().equals(department.getId()) && !author.getId().equals(user.getId())){
                    return false;
                }
                break;
            // 公司 (允许访问所有合同)
            case 2:
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public Boolean hasApprovalPermission(Contract contract) {
        if (contract == null){
            return false;
        }
        // 获取合同作者
        User author = userService.getById(contract.getAuthorId());
        if (author == null){
            return false;
        }
        // 获取作者部门
        Department authorDepartment = departmentService.getById(author.getDepartmentId());
        if (authorDepartment == null){
            return false;
        }
        // 获取当前用户
        User user = userService.getAuthentication();
        if (user == null){
            return false;
        }
        // 获取当前用户部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 根据用户部门审批权限决定处理方式
        Integer status = contract.getStatus();
        switch (department.getPermissionApproval()){
            // 禁止
            case 0:
                return false;
            // 部门 (仅允许审批当前部门合同，且合同状态为部门审核)
            case 1:
                if (!authorDepartment.getId().equals(department.getId()) || !status.equals(Status.APPROVAL_DEPARTMENT)){
                    return false;
                }
                break;
            // 公司 (允许审批全部合同，但合同状态为公司审核)
            case 2:
                if (!status.equals(Status.APPROVAL_COMPANY)){
                    return false;
                }
                break;
            // 最终 (允许审批全部合同，允许跨级审批)
            case 3:
                if (!status.equals(Status.APPROVAL_DEPARTMENT) && !status.equals(Status.APPROVAL_COMPANY)
                        && !status.equals(Status.APPROVAL_FINAL)){
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public Map<String, Object> getInfo(Integer id) throws ContractException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> result = new HashMap<>();
        // 尝试获取合同
        Contract contract = ((ContractServiceImpl) AopContext.currentProxy()).getById(id);
        // 合法性检查
        if (contract == null){
            throw ContractException.PermissionDeny();
        }
        // 权限检查
        if (!((ContractServiceImpl) AopContext.currentProxy()).hasAccessPermission(contract) &&
                !((ContractServiceImpl) AopContext.currentProxy()).hasApprovalPermission(contract)){
            throw ContractException.PermissionDeny();
        }
        // 合同基本信息详情
        Map<String, Object> contractDetails = new HashMap<>();
        {
            contractDetails.put("id", contract.getId());
            contractDetails.put("name", contract.getName());
            contractDetails.put("type", contract.getType());
            contractDetails.put("status", contract.getStatus());
            contractDetails.put("start_date", contract.getStartDate() != null ? simpleDateFormat.format(contract.getStartDate()) : "");
            contractDetails.put("end_date", contract.getEndDate() != null ? simpleDateFormat.format(contract.getEndDate()) : "");
            contractDetails.put("period_start_date", contract.getPeriodStartDate() != null ? simpleDateFormat.format(contract.getPeriodStartDate()) : "");
            contractDetails.put("period_expire_date", contract.getEndDate() != null ? simpleDateFormat.format(contract.getEndDate()) : "");
            // 合同作者用户对象
            User authorUser = null;
            if (contract.getAuthorId() != null){
                authorUser = userService.getById(contract.getAuthorId());
            }
            contractDetails.put("author_name", authorUser != null ? authorUser.getName() : "");
            contractDetails.put("party", contract.getParty());
            contractDetails.put("funds", contract.getFunds());
        }
        result.put("contractDetails", contractDetails);
        // 合同文件
        Map<String, Object> contractFile = new HashMap<>();
        {
            // 获取合同文件url
            List<ContractAttachment> list = contractAttachmentService.getByContractId(contract.getId(), FileType.FILE);
            if (!list.isEmpty()){
                ContractAttachment attachment = list.get(0);
                // 读取合同文件
                try(FileInputStream fileInputStream = new FileInputStream(attachment.getUrl())) {
                    byte[] bytes = fileInputStream.readAllBytes();
                    // 将文件内容放入应答数据中
                    contractFile.put("file", "data:application/pdf;base64,"+ Base64.getEncoder().encodeToString(bytes));
                }catch (Exception ignored){}
            }
        }
        result.put("contractFile", contractFile);
        // 合同附件
        List<Object> contractAttachments = new ArrayList<>();
        {
            // 获取合同附件列表
            List<ContractAttachment> attachments = contractAttachmentService.getByContractId(contract.getId(), FileType.ATTACHMENT);
            attachments.forEach(attachment -> contractAttachments.add(Map.of(
                    "id", attachment.getId(),
                    "name", attachment.getName(),
                    "size", attachment.getSize()
            )));
        }
        result.put("contractAttachments", contractAttachments);
        // 合同盖章信息
        Map<String, Object> contractSeal = new HashMap<>();
        {
            Integer authorId = contract.getSealAuthorId();
            if (authorId != null){
                User sealer = userService.getById(contract.getSealAuthorId());
                contractSeal.put("seal_operator", sealer == null ? "-" : sealer.getName());
                contractSeal.put("seal_note", contract.getSealNote());
            }
        }
        result.put("contractSeal", contractSeal);
        // 合同扫描件
        Map<String, Object> contractScanFile = new HashMap<>();
        {
            // 获取合同文件url
            List<ContractAttachment> list = contractAttachmentService.getByContractId(contract.getId(), FileType.SCAN);
            if (!list.isEmpty()){
                ContractAttachment attachment = list.get(0);
                // 读取合同文件
                try(FileInputStream fileInputStream = new FileInputStream(attachment.getUrl())) {
                    byte[] bytes = fileInputStream.readAllBytes();
                    // 将文件内容放入应答数据中
                    contractScanFile.put("file", "data:application/pdf;base64,"+Base64.getEncoder().encodeToString(bytes));
                }catch (Exception ignored){}
            }
        }
        result.put("contractScanFile", contractScanFile);
        // 补充协议列表
        List<Object> contractSupplements = new ArrayList<>();
        {
            // 获取全部补充协议
            List<Supplement> list = supplementService.getByContractId(contract.getId());
            list.forEach(supplement -> {
                // 仅显示通过审批的协议
                Integer status = supplement.getStatus();
                if (!status.equals(Status.RECORD_PASS) && !status.equals(Status.ENDED)){
                    return;
                }
                contractSupplements.add(Map.of(
                    "id", supplement.getId(),
                    "name", supplement.getName(),
                    "createTime", supplement.getCreateTime() != null ? simpleDateFormat.format(supplement.getCreateTime()) : ""
                ));
            });
        }
        result.put("contractSupplements", contractSupplements);
        // 合同终结信息
        Map<String, Object> contractEndDetails = new HashMap<>();
        {
            contractEndDetails.put("note", contract.getEndNote());
        }
        result.put("contractEndDetails", contractEndDetails);
        // 合同终结附件
        List<Object> contractEndFiles = new ArrayList<>();
        {
            // 获取合同附件列表
            List<ContractAttachment> attachments = contractAttachmentService.getByContractId(contract.getId(), FileType.END_ATTACHMENT);
            attachments.forEach(attachment -> contractEndFiles.add(Map.of(
                    "id", attachment.getId(),
                    "name", attachment.getName(),
                    "size", attachment.getSize()
            )));
        }
        result.put("contractEndFiles", contractEndFiles);
        // 合同变更历史
        List<Object> contractOperateHistory = new ArrayList<>();
        {
            List<ContractRecord> records = contractRecordService.getAll(contract.getId());
            records.forEach(contractRecord -> {
                User operator = userService.getById(contractRecord.getOperatorId());
                contractOperateHistory.add(Map.of(
                        "operator", operator != null ? operator.getName() : "",
                        "operationType", contractRecord.getType(),
                        "time", contractRecord.getTime() != null ? simpleDateFormat.format(contractRecord.getTime()) : "",
                        "details", contractRecord.getDetails() != null ? contractRecord.getDetails() : ""
                ));
            });
        }
        result.put("contractOperateHistory", contractOperateHistory);
        // 返回数据
        return result;
    }

    @Override
    public Boolean isSelf(Contract contract) {
        if (contract == null){
            return false;
        }
        User user = userService.getAuthentication();
        if (user == null || user.getId() == null){
            return false;
        }
        return user.getId().equals(contract.getAuthorId());
    }
}
