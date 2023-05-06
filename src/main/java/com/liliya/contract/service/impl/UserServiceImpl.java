package com.liliya.contract.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liliya.contract.model.domain.Department;
import com.liliya.contract.dao.UserMapper;
import com.liliya.contract.model.domain.User;
import com.liliya.contract.model.exception.UserException;
import com.liliya.contract.model.response.LayuiFormDataResponse;
import com.liliya.contract.service.UserService;
import com.liliya.contract.utils.MailUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private DepartmentServiceImpl departmentService;

    @Resource
    private EmailServiceImpl emailService;

    @Override
    @Caching(put = {@CachePut(cacheNames = "user-id", key = "#result.getId()"),
            @CachePut(cacheNames = "user-name", key = "#name"),
            @CachePut(cacheNames = "user-email", key = "#email")})
    public User add(String name, String password, String email, Integer departmentId) throws UserException {
        // 用户名判空
        if (name == null || name.isEmpty()){
            throw UserException.NameNull();
        }
        // 用户名重复
        if(getByName(name) != null){
            throw UserException.NameDuplicated();
        }
        // 邮箱合法性判断
        if(!MailUtils.isEmail(email)){
            throw UserException.EmailInvalid();
        }
        // 邮箱重复
        if(getByEmail(email) != null){
            throw UserException.EmailDuplicated();
        }
        // 部门验证
        if (departmentService.getById(departmentId) == null){
            throw UserException.DepartmentNotFound();
        }
        // 密码加密
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // 新建用户
        User user = User
                .builder()
                .name(name)
                .password(encoder.encode(password))
                .email(email)
                .departmentId(departmentId)
                .build();
        // 插入
        if(userMapper.insert(user) < 1){
            throw UserException.AddFail();
        }
        return user;
    }

    @Override
    @Caching(evict = {@CacheEvict(cacheNames = "user-id", key = "#id"),
            @CacheEvict(cacheNames = "user-name", key = "#result.getName()"),
            @CacheEvict(cacheNames = "user-email", key = "#result.getEmail()")})
    public User delete(Integer id) throws UserException {
        // 不允许删除自己
        User authUser = getAuthentication();
        if (authUser == null){
            throw UserException.UserNotFound();
        }
        if (authUser.getId().equals(id)){
            throw UserException.NotAllowDeleteSelf();
        }
        // 删除
        User user = ((UserServiceImpl)AopContext.currentProxy()).getById(id);
        if (userMapper.deleteById(id) < 1){
            throw UserException.DeleteFail();
        }
        return user;
    }

    @Override
    @Caching(put = {@CachePut(cacheNames = "user-id", key = "#id"),
            @CachePut(cacheNames = "user-name", key = "#result.getName()"),
            @CachePut(cacheNames = "user-email", key = "#result.getEmail()")})
    public User update(Integer id, String name, String password, String email, Integer departmentId) throws UserException {
        // 查找用户
        User user = ((UserServiceImpl)AopContext.currentProxy()).getById(id);
        if (user == null){
            throw UserException.UserNotFound();
        }
        // 用户名判空
        if (name != null && !name.isEmpty()){
            // 用户名重复
            if(!user.getName().equals(name) && getByName(name) != null){
                throw UserException.NameDuplicated();
            }
            user.setName(name);
        }
        // 邮箱判空
        if (email != null && !email.isEmpty()){
            // 邮箱合法性判断
            if(!MailUtils.isEmail(email)){
                throw UserException.EmailInvalid();
            }
            // 邮箱重复
            if(!user.getEmail().equals(email) && getByEmail(email) != null){
                throw UserException.EmailDuplicated();
            }
            user.setEmail(email);
        }
        // 部门验证
        if (departmentService.getById(departmentId) == null){
            throw UserException.DepartmentNotFound();
        }
        // 密码加密
        if (password != null && !password.isEmpty()){
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            user.setPassword(encoder.encode(password));
        }
        // 更新数据
        user.setDepartmentId(departmentId);
        // 插入
        if(userMapper.updateById(user) < 1){
            throw UserException.UpdateFail();
        }
        return user;
    }

    @Override
    public User getAuthentication(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        User user = ((UserServiceImpl)AopContext.currentProxy()).getById(Integer.parseInt(authentication.getName()));
        // 获取用户失败时应取消认证
        if (user == null) {
            SecurityContextHolder.clearContext();
        }
        return user;
    }

    @Override
    public Map<String, Object> getAuthenticationInfo(){
        Map<String, Object> map = new HashMap<>();
        // 获取当前用户
        User user = getAuthentication();
        if (user == null){
            return map;
        }
        // 组装信息
        map.put("user_name", user.getName());
        map.put("user_email", user.getEmail());
        // 部门ID为空时，返回无部门信息
        if (user.getDepartmentId() == null){
            map.put("department_name", "无");
            return map;
        }
        // 获取用户部门名称
        Department department = departmentService.getById(user.getDepartmentId());
        if(department != null){
            map.put("department_name", department.getName());
        }
        // 返回信息
        return map;
    }

    @Override
    @Cacheable(cacheNames = "user-id", key = "#id", unless="#result == null")
    public User getById(Integer id){
        return userMapper.selectById(id);
    }

    @Override
    @Cacheable(cacheNames = "user-name", key = "#name", unless="#result == null")
    public User getByName(String name) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("user_name", name));
    }

    @Override
    @Cacheable(cacheNames = "user-email", key = "#email", unless="#result == null")
    public User getByEmail(String email) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("user_email", email));
    }

    @Override
    public List<User> getByDepartmentId(Integer departmentId) {
        return userMapper.selectList(new QueryWrapper<User>().eq("user_department_id", departmentId));
    }

    @Override
    public LayuiFormDataResponse<Object> getPage(Integer page, Integer limit, Integer searchType, String searchText) {
        // 根据传入参数建立 wrapper
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        switch (searchType){
            // 筛选ID
            case 1:
                wrapper.eq("user_id", searchText);
                break;
            // 筛选用户名
            case 2:
                wrapper.like("user_name", searchText);
                break;
            // 筛选邮箱
            case 3:
                wrapper.eq("user_email", searchText);
                break;
            // 筛选部门名称
            case 4:
                Department department = departmentService.getByName(searchText);
                if (department == null){
                    return LayuiFormDataResponse.ok("成功返回", 0L, new ArrayList<>());
                }
                wrapper.eq("user_department_id", department.getId());
                break;
            // 不进行筛选
            default:
                break;
        }
        // 限制最大查询数
        if (limit > 100){
            limit = 100;
        }
        // 查询分页数据
        Page<User> queryPage = userMapper.selectPage(new Page<>(page, limit), wrapper);
        // 组装用户信息
        List<Map<String, Object>> result = new ArrayList<>();
        queryPage.getRecords().forEach(user -> {
            Integer departmentId = user.getDepartmentId();
            result.add(Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "department", departmentService.getById(departmentId).getName(),
                    "departmentId", departmentId
            ));
        });
        return LayuiFormDataResponse.ok("成功返回", queryPage.getTotal(), result);
    }

    @Override
    public Boolean sendResetPasswordVerifyCode(String email) {
        return emailService.sendVerifyCode("FORGET_PASSWORD_VERIFY", email);
    }

    @Override
    public void resetPassword(String email, String code, String password) throws UserException {
        // 获取此用户
        User user = ((UserServiceImpl)AopContext.currentProxy()).getByEmail(email);
        // 再次验证用户是否存在
        if(user == null){
           throw UserException.EmailInvalid();
        }
        // 验证密码是否为空
        if(password == null || password.isEmpty()){
            throw UserException.PasswordNull();
        }
        // 验证邮箱与验证码
        if(emailService.checkVerifyCode("FORGET_PASSWORD_VERIFY", email, code)){
            // 尝试更新用户
            ((UserServiceImpl)AopContext.currentProxy()).update(user.getId(), user.getName(), password, user.getEmail(),
                    user.getDepartmentId());
        }else {
            throw UserException.InvalidVerifyCode();
        }
    }
}
