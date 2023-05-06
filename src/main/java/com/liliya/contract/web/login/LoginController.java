package com.liliya.contract.web.login;

import com.liliya.contract.model.exception.UserException;
import com.liliya.contract.service.impl.UserServiceImpl;
import com.liliya.contract.model.response.SimpleResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping("/login")
public class LoginController {
    @Resource
    private UserServiceImpl userService;

    @GetMapping
    public String web() {
        return "login";
    }

    @PostMapping("/sendVerifyCode")
    @ResponseBody
    public SimpleResponse<Object> sendVerifyCode(@RequestParam String email) {
        if(userService.sendResetPasswordVerifyCode(email)){
            return SimpleResponse.ok("如果邮箱无误，您会收到一封携带验证码的邮件");
        }
        return SimpleResponse.fail("验证码发送失败，请稍后再试");
    }

    @PostMapping("/requestChange")
    @ResponseBody
    public SimpleResponse<Object> requestPasswordChange(@RequestParam String email, @RequestParam String code, @RequestParam String password) {
        try {
            userService.resetPassword(email, code, password);
            return SimpleResponse.ok("密码修改成功");
        }catch (UserException e){
            return SimpleResponse.fail(e.getMessage());
        }
    }
}
