package com.liliya.contract.service;

import com.liliya.contract.model.domain.Supplement;
import com.liliya.contract.model.exception.SupplementException;
import com.liliya.contract.model.response.LayuiFormDataResponse;

import java.util.List;
import java.util.Map;

public interface SupplementService {
    // 新增协议
    Supplement add(Integer contractId) throws SupplementException;

    // 删除协议
    Supplement delete(Integer id) throws SupplementException;

    // 更新协议
    Supplement update(Integer id, String name) throws SupplementException;

    // 更新协议
    Supplement update(Supplement supplement) throws SupplementException;

    // 根据 ID 获取协议
    Supplement getById(Integer id);

    // 根据合同 ID 获取协议
    List<Supplement> getByContractId(Integer contractId);

    // 分页条件查询协议
    LayuiFormDataResponse<Object> getPage(Integer pageType, Integer page, Integer limit, Integer status,
                                          String name, Integer contractId);

    // 检查当前用户访问权限
    Boolean hasAccessPermission(Supplement supplement);

    // 检查当前用户审批权限
    Boolean hasApprovalPermission(Supplement supplement);

    // 检查作者是否为当前用户
    Boolean isSelf(Supplement supplement);

    // 获取前端所需的合同信息
    Map<String, Object> getInfo(Integer id) throws SupplementException;
}
