package com.liliya.contract.model.exception;

public class DepartmentException extends Exception{

    public DepartmentException(String str) {
        super(str);
    }

    public static DepartmentException NameDuplicated(){return new DepartmentException("名称已存在");}

    public static DepartmentException NameNull(){return new DepartmentException("名称为空");}

    public static DepartmentException DepartmentNotFound(){
        return new DepartmentException("部门不存在");
    }

    public static DepartmentException AddFail(){
        return new DepartmentException("新增部门失败");
    }

    public static DepartmentException UpdateFail(){
        return new DepartmentException("更新部门失败");
    }

    public static DepartmentException DeleteFail(){
        return new DepartmentException("删除部门失败");
    }

    public static DepartmentException HasMember(){
        return new DepartmentException("当前部门存在成员");
    }
}
