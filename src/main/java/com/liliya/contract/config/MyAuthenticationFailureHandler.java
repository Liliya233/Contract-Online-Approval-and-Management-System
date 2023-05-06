package com.liliya.contract.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, AuthenticationException e) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("ok", false);
        if(e.getMessage().equals("Bad credentials")){
            result.put("message", "登录失败: 用户名或密码错误");
        }else{
            result.put("message", "登录失败: " + e.getMessage());
        }
        response.setContentType("application/json;charset=UTF-8");
        String s = new ObjectMapper().writeValueAsString(result);
        response.getWriter().println(s);
    }
}