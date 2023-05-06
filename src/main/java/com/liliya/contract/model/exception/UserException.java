package com.liliya.contract.model.exception;

public class UserException extends Exception{

    public UserException(String str) {
        super(str);
    }

    public static UserException EmailDuplicated(){
        return new UserException("邮箱已存在");
    }

    public static UserException EmailInvalid(){
        return new UserException("邮箱格式不正确");
    }

    public static UserException NameDuplicated(){return new UserException("名称已存在");}

    public static UserException NameNull(){return new UserException("名称为空");}

    public static UserException UserNotFound(){
        return new UserException("用户不存在");
    }

    public static UserException DepartmentNotFound(){
        return new UserException("部门不存在");
    }

    public static UserException AddFail(){
        return new UserException("新增用户失败");
    }

    public static UserException UpdateFail(){
        return new UserException("更新用户失败");
    }

    public static UserException DeleteFail(){
        return new UserException("删除用户失败");
    }

    public static UserException NotAllowDeleteSelf(){
        return new UserException("不允许删除自己");
    }

    public static UserException PasswordNull(){
        return new UserException("密码不能为空");
    }

    public static UserException InvalidVerifyCode(){return new UserException("密码不能为空");}
}
