package com.liliya.contract.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liliya.contract.dao.ContractRecordVoMapper;
import com.liliya.contract.model.define.OperationType;
import com.liliya.contract.model.domain.Department;
import com.liliya.contract.model.domain.User;
import com.liliya.contract.model.vo.ContractRecordVo;
import com.liliya.contract.service.ChartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ChartServiceImpl implements ChartService {
    @Resource
    private UserServiceImpl userService;

    @Resource
    private DepartmentServiceImpl departmentService;

    @Resource
    private ContractRecordVoMapper contractRecordVoMapper;

    @Override
    public Long getTotalByOperationType(Integer type, Date start, Date end) {
        // 获取用户
        User user = userService.getAuthentication();
        if (user == null){
            return 0L;
        }
        // 获取部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 查询参数
        QueryWrapper<ContractRecordVo> wrapper = new QueryWrapper<>();
        // 设置日期
        wrapper.ge("record_time", start);
        wrapper.le("record_time", end);
        // 指定类型的记录
        wrapper.eq("record_operation_type", type);
        // 根据部门权限返回不同数据
        switch (department.getPermissionAccess()){
            // 个人
            case 0:
                // 返回当前用户数据
                wrapper.eq("user_id", user.getId());
                // 返回查询总数
                return contractRecordVoMapper.getTotal(wrapper);
            // 部门
            case 1:
                // 返回当前部门数据
                wrapper.eq("user_department_id", user.getDepartmentId());
                // 返回查询总数
                return contractRecordVoMapper.getTotal(wrapper);
            // 公司
            case 2:
                // 返回查询总数
                return contractRecordVoMapper.getTotal(wrapper);
        }
        return 0L;
    }

    @Override
    public Long getTotalFunds(Date start, Date end) {
        // 获取用户
        User user = userService.getAuthentication();
        if (user == null){
            return 0L;
        }
        // 获取部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 查询参数
        QueryWrapper<ContractRecordVo> wrapper = new QueryWrapper<>();
        // 设置日期
        wrapper.ge("record_time", start);
        wrapper.le("record_time", end);
        // 备案通过的记录
        wrapper.eq("record_operation_type", OperationType.RECORD_ALLOW);
        // 根据部门权限返回不同数据
        switch (department.getPermissionAccess()){
            // 个人
            case 0:
                // 返回当前用户数据
                wrapper.eq("user_id", user.getId());
                // 返回金额总数
                return contractRecordVoMapper.getTotalFunds(wrapper);
            // 部门
            case 1:
                // 返回当前部门数据
                wrapper.eq("user_department_id", user.getDepartmentId());
                // 返回金额总数
                return contractRecordVoMapper.getTotalFunds(wrapper);
            // 公司
            case 2:
                // 返回金额总数
                return contractRecordVoMapper.getTotalFunds(wrapper);
        }
        return 0L;
    }

    @Override
    public List<Map<String, Object>> getDepartmentRankByFunds(Date start, Date end) {
        // 获取用户
        User user = userService.getAuthentication();
        if (user == null){
            return null;
        }
        // 获取部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 权限验证
        if (department.getPermissionAccess() < 2){
            return null;
        }
        // 查询参数
        QueryWrapper<ContractRecordVo> wrapper = new QueryWrapper<>();
        // 设置日期
        wrapper.ge("record_time", start);
        wrapper.le("record_time", end);
        // 分组排序
        wrapper.groupBy("department_id");
        wrapper.orderByDesc("su");
        // 数量限制
        Page<Map<String, Object>> queryPage = new Page<>(1, 10);
        // 查询返回
        return contractRecordVoMapper.getDepartmentRankByFunds(queryPage, wrapper).getRecords();
    }

    @Override
    public List<Map<String, Object>> getDepartmentRankByEnd(Date start, Date end) {
        // 获取用户
        User user = userService.getAuthentication();
        if (user == null){
            return null;
        }
        // 获取部门
        Department department = departmentService.getById(user.getDepartmentId());
        // 权限验证
        if (department.getPermissionAccess() < 2){
            return null;
        }
        // 查询参数
        QueryWrapper<ContractRecordVo> wrapper = new QueryWrapper<>();
        // 设置日期
        wrapper.ge("record_time", start);
        wrapper.le("record_time", end);
        // 操作类型
        wrapper.eq("record_operation_type", OperationType.END_ALLOW);
        // 分组排序
        wrapper.groupBy("department_id");
        wrapper.orderByDesc("su");
        // 数量限制
        Page<Map<String, Object>> queryPage = new Page<>(1, 10);
        // 查询返回
        return contractRecordVoMapper.getDepartmentRankByOperationType(queryPage, wrapper).getRecords();
    }
}
