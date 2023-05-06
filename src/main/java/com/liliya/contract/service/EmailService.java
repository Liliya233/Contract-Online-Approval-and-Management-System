package com.liliya.contract.service;

public interface EmailService {
    // 若存在，发送忘记密码邮箱验证码
    boolean sendVerifyCode(String prefix, String email);

    // 验证忘记密码邮箱验证码
    boolean checkVerifyCode(String prefix, String email, String code);
}
