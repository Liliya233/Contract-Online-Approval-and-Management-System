package com.liliya.contract.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liliya.contract.model.domain.Department;
import com.liliya.contract.dao.DepartmentMapper;
import com.liliya.contract.model.exception.DepartmentException;
import com.liliya.contract.model.response.LayuiFormDataResponse;
import com.liliya.contract.service.DepartmentService;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private UserServiceImpl userService;

    @Override
    @Caching(put = {@CachePut(cacheNames = "department-id", key = "#result.getId()"),
            @CachePut(cacheNames = "department-name", key = "#name")})
    public Department add(String name, String admin, String create, Integer approval, String seal, String terminate,
                          Integer access) throws DepartmentException {
        // 名称为空
        if (name == null || name.isEmpty()){
            throw DepartmentException.NameNull();
        }
        // 部门名称重复
        if(((DepartmentServiceImpl)AopContext.currentProxy()).getByName(name) != null){
            throw DepartmentException.NameDuplicated();
        }
        // 新建部门对象
        Department department = Department
                .builder()
                .name(name)
                .permissionAdmin(admin != null && !admin.isEmpty())
                .permissionCreate(create != null && !create.isEmpty())
                .permissionApproval(approval)
                .permissionSeal(seal != null && !seal.isEmpty())
                .permissionTerminate(terminate != null && !terminate.isEmpty())
                .permissionAccess(access)
                .build();
        // 插入
        if(departmentMapper.insert(department) < 1){
            throw DepartmentException.AddFail();
        }
        return department;
    }

    @Override
    @Caching(evict = {@CacheEvict(cacheNames = "department-id", key = "#id"),
            @CacheEvict(cacheNames = "department-name", key = "#result.getName()")})
    public Department delete(Integer id) throws DepartmentException {
        // 不允许删除存在成员的部门
        if(!userService.getByDepartmentId(id).isEmpty()){
            throw DepartmentException.HasMember();
        }
        // 尝试删除
        Department department = ((DepartmentServiceImpl)AopContext.currentProxy()).getById(id);
        if (departmentMapper.deleteById(department) < 1){
            throw DepartmentException.DeleteFail();
        }
        return department;
    }

    @Override
    @Caching(put = {@CachePut(cacheNames = "department-id", key = "#id"),
            @CachePut(cacheNames = "department-name", key = "#name")})
    public Department update(Integer id, String name, String admin, String create, Integer approval, String seal,
                             String terminate, Integer access) throws DepartmentException {
        // 尝试查找
        Department department = ((DepartmentServiceImpl)AopContext.currentProxy()).getById(id);
        if(department == null){
            throw DepartmentException.DepartmentNotFound();
        }
        // 名称为空
        if (name == null || name.isEmpty()){
            throw DepartmentException.NameNull();
        }
        // 部门名称重复，且不为当前部门
        if(!department.getName().equals(name) && ((DepartmentServiceImpl)AopContext.currentProxy()).getByName(name) != null){
            throw DepartmentException.NameDuplicated();
        }
        // 更新信息
        department.setName(name);
        department.setPermissionAdmin(admin != null && !admin.isEmpty());
        department.setPermissionCreate(create != null && !create.isEmpty());
        department.setPermissionApproval(approval);
        department.setPermissionSeal(seal != null && !seal.isEmpty());
        department.setPermissionTerminate(terminate != null && !terminate.isEmpty());
        department.setPermissionAccess(access);
        // 插入
        if(departmentMapper.updateById(department) < 1){
            throw DepartmentException.UpdateFail();
        }
        return department;
    }

    @Override
    public List<Department> getAll(){
        return departmentMapper.selectList(null);
    }

    @Override
    @Cacheable(cacheNames = "department-id", key = "#id", unless = "#result == null")
    public Department getById(Integer id){
        return departmentMapper.selectById(id);
    }

    @Override
    @Cacheable(cacheNames = "department-name", key = "#name", unless = "#result == null")
    public Department getByName(String name){
        return departmentMapper.selectOne(new QueryWrapper<Department>().eq("department_name", name));
    }

    @Override
    public LayuiFormDataResponse<Object> getPage(Integer page, Integer limit, Integer searchType, String searchText) {
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        // 根据搜索类型确定 wrapper
        wrapper.eq(searchType.equals(1), "department_id", searchText)
                .like(searchType.equals(2), "department_name", searchText);
        // 限制最大查询数
        if (limit > 100){
            limit = 100;
        }
        // 查询
        Page<Department> queryPage = departmentMapper.selectPage(new Page<>(page, limit), wrapper);
        // 返回结果
        return LayuiFormDataResponse.ok("成功返回", queryPage.getTotal(), queryPage.getRecords());
    }
}
