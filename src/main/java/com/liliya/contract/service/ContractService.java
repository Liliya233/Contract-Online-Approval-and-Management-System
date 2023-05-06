package com.liliya.contract.service;

import com.liliya.contract.model.domain.Contract;
import com.liliya.contract.model.exception.ContractException;
import com.liliya.contract.model.response.LayuiFormDataResponse;

import java.util.Date;
import java.util.Map;

public interface ContractService {
    // 新增合同
    Contract add() throws ContractException;

    // 删除合同
    Contract delete(Integer id) throws ContractException;

    // 更新合同
    Contract update(Integer id, String name, Integer type, Date startDate, Date endDate, Date periodStartDate,
                    Date periodExpireDate, String party, Long funds) throws ContractException;

    // 根据 ID 获取合同
    Contract getById(Integer id);

    // 分页条件查询合同
    LayuiFormDataResponse<Object> getPage(Integer pageType, Integer page, Integer limit, Integer type, Integer status,
                                          String name, String author, String party);

    // 检查当前用户访问权限
    Boolean hasAccessPermission(Contract contract);

    // 检查当前用户审批权限
    Boolean hasApprovalPermission(Contract contract);

    // 获取前端所需的合同信息
    Map<String, Object> getInfo(Integer id) throws ContractException;

    // 检查作者是否为当前用户
    Boolean isSelf(Contract contract);
}
