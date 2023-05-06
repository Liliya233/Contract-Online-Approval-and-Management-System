package com.liliya.contract.service.impl;

import com.liliya.contract.model.domain.Department;
import com.liliya.contract.model.domain.User;
import com.liliya.contract.utils.MailUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Resource
    private UserServiceImpl userService;

    @Resource
    private DepartmentServiceImpl departmentService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String str) throws UsernameNotFoundException {
        // 根据传入字符串类型来获取用户
        User user = null;
        // ID 登录
        try{
            user = userService.getById(Integer.parseInt(str));
        }catch (NumberFormatException ignored){}
        // 邮箱和名称登录
        if (user == null){
            if (MailUtils.isEmail(str)){
                user = userService.getByEmail(str);
            }else {
                user = userService.getByName(str);
            }
        }
        // 如果查询的用户不存在（用户名与邮箱都不存在），必须抛出此异常
        if (user == null){
            throw new UsernameNotFoundException("当前用户不存在！");
        }
        // 用户部门为空时，返回无权限数据
        if (user.getDepartmentId() == null){
            return new org.springframework.security.core.userdetails.User(user.getId().toString(), user.getPassword(), new ArrayList<>());
        }
        // 获取用户权限 (所属部门权限)
        Department department = departmentService.getById(user.getDepartmentId());
        List<SimpleGrantedAuthority> authority = getSimpleGrantedAuthorityList(department);
        // 返回封装的数据
        return new org.springframework.security.core.userdetails.User(user.getId().toString(), user.getPassword(), authority);
    }

    // 提取部门权限
    public static List<SimpleGrantedAuthority> getSimpleGrantedAuthorityList(Department department){
        List<SimpleGrantedAuthority> list = new ArrayList<>();
        if (department == null){
            return list;
        }
        if (department.isPermissionAdmin()){
            list.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if (department.isPermissionCreate()){
            list.add(new SimpleGrantedAuthority("ROLE_CREATE"));
        }
        if (department.getPermissionApproval() > 0){
            list.add(new SimpleGrantedAuthority("ROLE_APPROVAL"));
        }
        if (department.isPermissionSeal()){
            list.add(new SimpleGrantedAuthority("ROLE_SEAL"));
        }
        if (department.isPermissionTerminate()){
            list.add(new SimpleGrantedAuthority("ROLE_TERMINATE"));
        }
        if (department.getPermissionAccess() > 1){
            list.add(new SimpleGrantedAuthority("ROLE_ACCESS"));
        }
        return list;
    }
}
