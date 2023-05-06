package com.liliya.contract.model.exception;

public class ContractException extends Exception{

    public ContractException(String str) {
        super(str);
    }

    public static ContractException AddFail(){
        return new ContractException("新增合同失败");
    }

    public static ContractException DeleteFail(){ return new ContractException("删除合同失败");}

    public static ContractException PermissionDeny(){
        return new ContractException("无权访问");
    }

    public static ContractException EditFail(){ return new ContractException("更新合同失败");}

    public static ContractException NotFinish(){ return new ContractException("请完成信息填写后再提交");}

    public static ContractException NotFile(){ return new ContractException("请上传或生成合同文件");}

    public static ContractException ApprovalFail(){ return new ContractException("审批失败");}

    public static ContractException ApprovalCancelFail(){ return new ContractException("当前合同不允许撤回审批");}

    public static ContractException ApprovalAllowFail(){ return new ContractException("同意合同审批失败");}

    public static ContractException SealFail(){ return new ContractException("合同盖章失败");}

    public static ContractException NotScan(){ return new ContractException("请上传合同扫描版文件");}
}
