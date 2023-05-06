package com.liliya.contract.model.exception;

public class SupplementException extends Exception{

    public SupplementException(String str) {
        super(str);
    }

    public static SupplementException AddFail(){
        return new SupplementException("新增补充协议失败");
    }

    public static SupplementException DeleteFail(){
        return new SupplementException("删除补充协议失败");
    }

    public static SupplementException PermissionDeny(){
        return new SupplementException("无权访问");
    }

    public static SupplementException EditFail(){
        return new SupplementException("更新补充协议失败");
    }

    public static SupplementException NotFinish(){
        return new SupplementException("请完成补充协议信息填写后再提交审批");
    }

    public static SupplementException NotFile(){
        return new SupplementException("请上传或生成补充协议文件");
    }

    public static SupplementException ApprovalFail(){
        return new SupplementException("审批失败");
    }

    public static SupplementException ApprovalCancelFail(){
        return new SupplementException("当前补充协议不允许撤回审批");
    }

    public static SupplementException ApprovalAllowFail(){
        return new SupplementException("同意补充协议审批失败");
    }

    public static SupplementException SealFail(){
        return new SupplementException("补充协议盖章失败");
    }

    public static SupplementException NotScan(){
        return new SupplementException("请上传补充协议扫描版文件");
    }
}
