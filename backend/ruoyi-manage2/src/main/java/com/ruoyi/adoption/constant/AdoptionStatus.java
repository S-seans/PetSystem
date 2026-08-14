package com.ruoyi.adoption.constant;

/**
 * 领养申请状态常量（与表 tb_adoption_request.status 存储值保持一致）
 * 
 * @author ruoyi
 */
public final class AdoptionStatus
{
    /** 待审核 */
    public static final String PENDING = "pending";

    /** 审核通过（待办理领养） */
    public static final String PASS = "pass";

    /** 已领养 */
    public static final String OUT = "out";

    /** 已拒绝 */
    public static final String REJECT = "reject";

    private AdoptionStatus()
    {
    }
}
