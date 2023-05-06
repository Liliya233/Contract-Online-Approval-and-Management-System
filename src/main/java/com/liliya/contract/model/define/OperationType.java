package com.liliya.contract.model.define;

public final class OperationType {
    /**
     * 新建业务
     */
    public static final int CREATE = 0;

    /**
     * 提交审批
     */
    public static final int APPROVAL_SUBMIT = 1;

    /**
     * 撤回审批
     */
    public static final int APPROVAL_DROP = 2;

    /**
     * 部门审批不通过
     */
    public static final int APPROVAL_DEPARTMENT_REJECT = 3;

    /**
     * 部门审批通过
     */
    public static final int APPROVAL_DEPARTMENT_ALLOW = 4;

    /**
     * 公司审批不通过
     */
    public static final int APPROVAL_COMPANY_REJECT = 5;

    /**
     * 公司审批通过
     */
    public static final int APPROVAL_COMPANY_ALLOW = 6;

    /**
     * 最终审批不通过
     */
    public static final int APPROVAL_FINAL_REJECT = 7;

    /**
     * 最终审批通过
     */
    public static final int APPROVAL_FINAL_ALLOW = 8;

    /**
     * 业务盖章
     */
    public static final int SEAL = 9;

    /**
     * 提交扫描版 (请求备案审核)
     */
    public static final int SCAN = 10;

    /**
     * 备案审核不通过
     */
    public static final int RECORD_FAIL = 11;

    /**
     * 备案审核通过
     */
    public static final int RECORD_ALLOW = 12;

    /**
     * 新增补充协议 (仅合同，协议备案通过时触发)
     */
    public static final int CREATE_SUPPLEMENT = 13;

    /**
     * 提交终结 (仅合同)
     */
    public static final int END_SUBMIT = 14;

    /**
     * 终结审批未通过 (仅合同)
     */
    public static final int END_REJECT = 15;

    /**
     * 终结审批通过
     */
    public static final int END_ALLOW = 16;
}
