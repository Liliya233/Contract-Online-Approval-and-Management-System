package com.liliya.contract.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liliya.contract.dao.SupplementMapper;
import com.liliya.contract.model.define.OperationType;
import com.liliya.contract.model.define.Status;
import com.liliya.contract.model.domain.*;
import com.liliya.contract.model.exception.SupplementException;
import com.liliya.contract.model.response.LayuiFormDataResponse;
import com.liliya.contract.service.ContractService;
import com.liliya.contract.service.SupplementRecordService;
import com.liliya.contract.service.SupplementService;
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
public class SupplementServiceImpl implements SupplementService {
    @Resource
    private SupplementMapper supplementMapper;

    @Resource
    private ContractService contractService;

    @Resource
    private UserServiceImpl userService;

    @Resource
    private SupplementRecordService supplementRecordService;

    @Resource
    private SupplementAttachmentServiceImpl supplementAttachmentService;

    @Resource
    private DepartmentServiceImpl departmentService;

    @Override
    @CachePut(cacheNames = "supplement-id", key = "#result.getId()")
    public Supplement add(Integer contractId) throws SupplementException{
        // 验证合同ID
        Contract contract = contractService.getById(contractId);
        // 仅允许合同负责人新建协议
        if (!contractService.isSelf(contract)){
            throw SupplementException.PermissionDeny();
        }
        // 验证合同状态
        Integer status = contract.getStatus();
        if (!status.equals(Status.RECORD_PASS) && !status.equals(Status.END_FAIL)){
            throw SupplementException.PermissionDeny();
        }
        // 新建合同对象
        Supplement supplement = Supplement
                .builder()
                .status(Status.APPROVAL_AWAIT)
                .contractId(contractId)
                .createTime(new Date(System.currentTimeMillis()))
                .build();
        // 插入
        if(supplementMapper.insert(supplement) < 1){
            throw SupplementException.AddFail();
        }
        // 新增记录
        SupplementRecord supplementRecord = SupplementRecord
                .builder()
                .type(OperationType.CREATE)
                .details("新增 " + supplement.getContractId() +" 号合同的补充协议")
                .build();
        supplementRecordService.add(supplement, supplementRecord);
        return supplement;
    }

    @Override
    @CacheEvict(cacheNames = "supplement-id", key = "#id")
    public Supplement delete(Integer id) throws SupplementException {
        // 获取当前协议
        Supplement supplement = ((SupplementServiceImpl) AopContext.currentProxy()).getById(id);
        // 合法性检查
        if (!((SupplementServiceImpl) AopContext.currentProxy()).hasAccessPermission(supplement)){
            throw SupplementException.PermissionDeny();
        }
        // 通过备案的协议不允许删除
        if(supplement.getStatus() >= Status.RECORD_PASS){
            throw SupplementException.DeleteFail();
        }
        if (supplementMapper.deleteById(id) < 1){
            throw SupplementException.DeleteFail();
        }
        return supplement;
    }

    @Override
    @CachePut(cacheNames = "supplement-id", key = "#id")
    public Supplement update(Integer id, String name) throws SupplementException {
        // 获取当前协议
        Supplement supplement = ((SupplementServiceImpl) AopContext.currentProxy()).getById(id);
        // 合法性检查
        if (!((SupplementServiceImpl) AopContext.currentProxy()).isSelf(supplement)){
            throw SupplementException.PermissionDeny();
        }
        // 仅允许修改未审核与审核不通过的协议
        Integer status = supplement.getStatus();
        if (!status.equals(Status.APPROVAL_AWAIT) && !status.equals(Status.APPROVAL_FAIL)){
            throw SupplementException.EditFail();
        }
        // 修改对象
        supplement.setName(name);
        // 插入
        if (supplementMapper.updateById(supplement) < 1){
            throw SupplementException.EditFail();
        }
        return supplement;
    }

    @Override
    @CachePut(cacheNames = "supplement-id", key = "#supplement.id")
    public Supplement update(Supplement supplement) throws SupplementException {
        // 插入
        if (supplementMapper.updateById(supplement) < 1){
            throw SupplementException.EditFail();
        }
        return supplement;
    }

    @Override
    @Cacheable(cacheNames = "supplement-id", key = "#id", unless = "#result == null")
    public Supplement getById(Integer id) {
        return supplementMapper.selectById(id);
    }

    @Override
    public List<Supplement> getByContractId(Integer contractId) {
        QueryWrapper<Supplement> wrapper = new QueryWrapper<>();
        // 将搜索条件放入 wrapper
        wrapper.eq("supplement_contract_id", contractId);
        // 查询
        return supplementMapper.selectList(wrapper);
    }

    @Override
    public LayuiFormDataResponse<Object> getPage(Integer pageType, Integer page, Integer limit, Integer status,
                                                 String name, Integer contractId) {
        List<Map<String, Object>> result = new ArrayList<>();
        QueryWrapper<Supplement> wrapper = new QueryWrapper<>();
        // 排序
        wrapper.orderByAsc("supplement_id");
        // 将搜索条件放入 wrapper
        wrapper.eq(status != null, "supplement_status", status)
                .like(name != null && !name.isEmpty(), "supplement_name", name)
                .eq(contractId != null, "supplement_contract_id", contractId);
        // 根据请求状态进行合法性检查
        User user = userService.getAuthentication();
        if (user == null){
            return LayuiFormDataResponse.ok("成功返回", 0L, result);
        }
        Department department = departmentService.getById(user.getDepartmentId());
        switch (pageType){
            // 仅获取有访问权限的协议
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
            // 仅获取有审批权限的协议
            case 1:
                switch (department.getPermissionApproval()){
                    // 禁止
                    case 0:
                        return LayuiFormDataResponse.ok("成功返回", 0L, result);
                    // 部门
                    case 1:
                        wrapper.eq("user_department_id", user.getDepartmentId());
                        wrapper.eq("supplement_status", Status.APPROVAL_DEPARTMENT);
                        break;
                    // 公司
                    case 2:
                        wrapper.between("supplement_status", Status.APPROVAL_DEPARTMENT, Status.APPROVAL_COMPANY);
                        break;
                    // 最终
                    case 3:
                        wrapper.between("supplement_status", Status.APPROVAL_DEPARTMENT, Status.APPROVAL_FINAL);
                        break;
                }
                break;
            // 仅获取等待盖章的协议
            case 2:
                wrapper.eq("supplement_status", Status.SEAL_AWAIT);
                break;
            // 仅获取等待备案的协议
            case 3:
                wrapper.eq("supplement_status", Status.RECORD_AWAIT);
                break;
            // 仅获取等待终结的协议
            case 4:
                wrapper.eq("supplement_status", Status.END_AWAIT);
                break;
        }
        // 协议信息获取
        Page<Supplement> newPage = new Page<>(page, limit);
        newPage.setOptimizeJoinOfCountSql(false);
        IPage<Supplement> queryPage = supplementMapper.findPageWithContractAndUserInfo(newPage, wrapper);
        // 组装协议信息
        queryPage.getRecords().forEach(supplement -> {
            // 获取所属合同
            Contract contract = contractService.getById(supplement.getContractId());
            assert contract != null;
            result.add(Map.of(
                    "id", supplement.getId(),
                    "name", supplement.getName() != null ? supplement.getName() : "",
                    "contractId", supplement.getContractId(),
                    "contractName", contract.getName(),
                    "status", supplement.getStatus(),
                    "self", contractService.isSelf(contract)
            ));
        });
        return LayuiFormDataResponse.ok("成功返回", queryPage.getTotal(), result);
    }

    @Override
    public Boolean hasAccessPermission(Supplement supplement) {
        Contract contract = contractService.getById(supplement.getContractId());
        if (contract != null){
            return contractService.hasAccessPermission(contract);
        }
        return false;
    }

    @Override
    public Boolean hasApprovalPermission(Supplement supplement) {
        if (supplement == null){
            return false;
        }
        // 尝试获取合同
        Contract contract = contractService.getById(supplement.getContractId());
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
        Integer status = supplement.getStatus();
        switch (department.getPermissionApproval()){
            // 禁止
            case 0:
                return false;
            // 部门 (仅允许审批当前部门协议，且协议状态为部门审核)
            case 1:
                if (!authorDepartment.getId().equals(department.getId()) || !status.equals(Status.APPROVAL_DEPARTMENT)){
                    return false;
                }
                break;
            // 公司 (允许审批全部协议，但协议状态为公司审核)
            case 2:
                if (!status.equals(Status.APPROVAL_COMPANY)){
                    return false;
                }
                break;
            // 最终 (允许审批全部协议，允许跨级审批)
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
    public Boolean isSelf(Supplement supplement) {
        if (supplement == null){
            return false;
        }
        return contractService.isSelf(contractService.getById(supplement.getContractId()));
    }

    @Override
    public Map<String, Object> getInfo(Integer id) throws SupplementException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> result = new HashMap<>();
        // 获取当前协议
        Supplement supplement = ((SupplementServiceImpl) AopContext.currentProxy()).getById(id);
        // 合法性检查
        if (supplement == null){
            throw SupplementException.PermissionDeny();
        }
        // 权限检查
        if (!((SupplementServiceImpl) AopContext.currentProxy()).hasAccessPermission(supplement)
                && !((SupplementServiceImpl) AopContext.currentProxy()).hasApprovalPermission(supplement)){
            throw SupplementException.PermissionDeny();
        }
        // 协议基本信息详情
        Map<String, Object> supplementDetails = new HashMap<>();
        {
            Integer contractId = supplement.getContractId();
            supplementDetails.put("id", supplement.getId());
            supplementDetails.put("name", supplement.getName());
            supplementDetails.put("contractId", contractId);
            supplementDetails.put("contractName", contractService.getById(contractId).getName());
            supplementDetails.put("status", supplement.getStatus());
        }
        result.put("supplementDetails", supplementDetails);
        // 协议文件
        Map<String, Object> supplementFile = new HashMap<>();
        {
            // 获取合同文件url
            List<SupplementAttachment> list = supplementAttachmentService.getBySupplementId(supplement.getId(), 0);
            if (!list.isEmpty()){
                SupplementAttachment attachment = list.get(0);
                // 读取合同文件
                try(FileInputStream fileInputStream = new FileInputStream(attachment.getUrl())) {
                    byte[] bytes = fileInputStream.readAllBytes();
                    // 将文件内容放入应答数据中
                    supplementFile.put("file", "data:application/pdf;base64,"+ Base64.getEncoder().encodeToString(bytes));
                }catch (Exception ignored){}
            }
        }
        result.put("supplementFile", supplementFile);
        // 协议附件
        List<Object> supplementAttachments = new ArrayList<>();
        {
            // 获取合同附件列表
            List<SupplementAttachment> attachments = supplementAttachmentService.getBySupplementId(supplement.getId(), 1);
            attachments.forEach(attachment -> supplementAttachments.add(Map.of(
                    "id", attachment.getId(),
                    "name", attachment.getName(),
                    "size", attachment.getSize()
            )));
        }
        result.put("supplementAttachments", supplementAttachments);
        // 合同盖章信息
        Map<String, Object> supplementSeal = new HashMap<>();
        {
            Integer authorId = supplement.getSealAuthorId();
            if (authorId != null){
                User sealer = userService.getById(supplement.getSealAuthorId());
                supplementSeal.put("seal_operator", sealer == null ? "-" : sealer.getName());
                supplementSeal.put("seal_note", supplement.getSealNote());
            }
        }
        result.put("supplementSeal", supplementSeal);
        // 合同扫描件
        Map<String, Object> supplementScanFile = new HashMap<>();
        {
            // 获取合同文件url
            List<SupplementAttachment> list = supplementAttachmentService.getBySupplementId(supplement.getId(), 2);
            if (!list.isEmpty()){
                SupplementAttachment attachment = list.get(0);
                // 读取合同文件
                try(FileInputStream fileInputStream = new FileInputStream(attachment.getUrl())) {
                    byte[] bytes = fileInputStream.readAllBytes();
                    // 将文件内容放入应答数据中
                    supplementScanFile.put("file", "data:application/pdf;base64,"+Base64.getEncoder().encodeToString(bytes));
                }catch (Exception ignored){}
            }
        }
        result.put("supplementScanFile", supplementScanFile);
        // 合同变更历史
        List<Object> supplementOperateHistory = new ArrayList<>();
        {
            List<SupplementRecord> records = supplementRecordService.getAll(supplement.getId());
            records.forEach(supplementRecord -> {
                User operator = userService.getById(supplementRecord.getOperatorId());
                supplementOperateHistory.add(Map.of(
                        "operator", operator != null ? operator.getName() : "",
                        "operationType", supplementRecord.getType(),
                        "time", supplementRecord.getTime() != null ? simpleDateFormat.format(supplementRecord.getTime()) : "",
                        "details", supplementRecord.getDetails() != null ? supplementRecord.getDetails() : ""
                ));
            });
        }
        result.put("supplementOperateHistory", supplementOperateHistory);
        // 返回数据
        return result;
    }
}
