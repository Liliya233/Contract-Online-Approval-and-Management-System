package com.liliya.contract.service;

import com.liliya.contract.model.domain.User;
import com.liliya.contract.model.exception.UserException;
import com.liliya.contract.model.response.LayuiFormDataResponse;

import java.util.List;
import java.util.Map;


public interface UserService {
    // 新增用户
    User add(String name, String password, String email, Integer departmentId) throws UserException;

    // 删除用户
    User delete(Integer id) throws UserException;

    // 更新用户
    User update(Integer id, String name, String password, String email, Integer departmentId) throws UserException;

    // 获取当前用户
    User getAuthentication();

    // 获取前端所需的当前用户信息
    Map<String, Object> getAuthenticationInfo();

    // 通过ID获取用户
    User getById(Integer id);

    // 通过名称获取用户
    User getByName(String name);

    // 通过邮箱获取用户
    User getByEmail(String email);

    // 通过部门ID获取用户
    List<User> getByDepartmentId(Integer id);

    // 分页条件查询用户
    LayuiFormDataResponse<Object> getPage(Integer page, Integer limit, Integer searchType, String searchText);

    // 发送重置密码邮箱验证码
    Boolean sendResetPasswordVerifyCode(String email);

    // 重置密码
    void resetPassword(String email, String code, String password) throws UserException;
}
