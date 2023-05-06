package com.liliya.contract.service;

import com.liliya.contract.model.domain.Department;
import com.liliya.contract.model.exception.DepartmentException;
import com.liliya.contract.model.response.LayuiFormDataResponse;

import java.util.List;

public interface DepartmentService {
    // 新增部门
    Department add(String name, String admin, String create, Integer approval, String seal, String terminate,
                   Integer access) throws DepartmentException;

    // 删除部门
    Department delete(Integer id) throws DepartmentException;

    // 更新部门
    Department update(Integer id, String name, String admin, String create, Integer approval, String seal,
                      String terminate, Integer access) throws DepartmentException;

    // 获取全部部门
    List<Department> getAll();

    // 通过ID获取部门
    Department getById(Integer id);

    // 通过名称获取部门
    Department getByName(String name);

    // 分页条件查询部门
    LayuiFormDataResponse<Object> getPage(Integer page, Integer limit, Integer searchType, String searchText);
}
