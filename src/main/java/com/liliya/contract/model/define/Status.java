package com.liliya.contract.model.define;

public final class Status {
    /**
     * 等待提交审批
     */
    public static final int APPROVAL_AWAIT = 0;

    /**
     * 部门审批中
     */
    public static final int APPROVAL_DEPARTMENT = 1;

    /**
     * 公司审批中
     */
    public static final int APPROVAL_COMPANY = 2;

    /**
     * 最终审批中
     */
    public static final int APPROVAL_FINAL = 3;

    /**
     * 审批未通过
     */
    public static final int APPROVAL_FAIL = 4;

    /**
     * 等待盖章
     */
    public static final int SEAL_AWAIT = 5;

    /**
     * 等待提交扫描版 (等待提交备案审核)
     */
    public static final int SCAN_AWAIT = 6;

    /**
     * 备案审核中
     */
    public static final int RECORD_AWAIT = 7;

    /**
     * 备案未通过
     */
    public static final int RECORD_FAIL = 8;

    /**
     * 备案通过
     */
    public static final int RECORD_PASS = 9;

    /**
     * 终结审批中 (仅合同)
     */
    public static final int END_AWAIT = 10;

    /**
     * 终结审批未通过 (仅合同)
     */
    public static final int END_FAIL = 11;

    /**
     * 合同已终结
     */
    public static final int ENDED = 12;
}
