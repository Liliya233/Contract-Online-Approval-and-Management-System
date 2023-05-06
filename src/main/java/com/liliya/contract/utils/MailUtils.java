package com.liliya.contract.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MailUtils {
    @Resource
    private JavaMailSenderImpl mailSender;
    @Value("${spring.mail.username}")
    private String mailFrom;

    // 发送简单邮件
    public boolean sendSimpleEmail(String mailTo, String title, String content) {
        //  定制邮件发送内容
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(mailTo);
        message.setSubject(title);
        message.setText(content);
        // 发送邮件
        try {
            mailSender.send(message);
        }catch(MailException e){
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean isEmail(String str){
        String check = "^([a-z0-9A-Z]+[-|]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(str);
        return matcher.matches();
    }
}

