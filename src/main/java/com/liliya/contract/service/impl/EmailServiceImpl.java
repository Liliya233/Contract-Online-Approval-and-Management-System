package com.liliya.contract.service.impl;

import com.liliya.contract.utils.MailUtils;
import com.liliya.contract.utils.ServletUtil;
import com.liliya.contract.service.EmailService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class EmailServiceImpl implements EmailService {
    @Resource
    private UserServiceImpl userService;

    @Resource
    private MailUtils mailUtils;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean sendVerifyCode(String prefix, String email) {
        // 冷却检查
        String coolDownKey = prefix + "_COOL_DOWN_" + ServletUtil.getRequest().getRemoteHost();
        if(redisTemplate.opsForValue().get(coolDownKey) != null){
            return false;
        }
        // 请求冷却设置
        redisTemplate.opsForValue().set(coolDownKey, "ok", 55, TimeUnit.SECONDS);
        // 检查邮箱格式
        if(!MailUtils.isEmail(email)){
            return false;
        }
        // 用户存在，且未在冷却期内时进行发送
        if(userService.getByEmail(email) != null){
            // 随机生成验证码
            Random rand = new Random();
            int verifyCode = rand.nextInt(899999) + 100000;
            // 发送验证码
            String content = "我们收到了您的验证码请求，以下为本次操作的验证码：\n\n" + verifyCode;
            if (mailUtils.sendSimpleEmail(email, "合同在线审批管理系统", content)){
                // 发送成功则使用 Redis 缓存验证码，同时进入请求冷却
                String key = prefix + "_CODE_" + email;
                redisTemplate.opsForValue().set(key, String.valueOf(verifyCode), 600, TimeUnit.SECONDS);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkVerifyCode(String prefix, String email, String code) {
        String key = prefix + "_CODE_" + email;
        Object obj = redisTemplate.opsForValue().get(key);
        if(obj != null && obj.toString().equals(code)){
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}