package com.liliya.contract.web.user;

import com.liliya.contract.model.domain.User;
import com.liliya.contract.model.exception.UserException;
import com.liliya.contract.service.impl.EmailServiceImpl;
import com.liliya.contract.service.impl.UserServiceImpl;
import com.liliya.contract.model.response.SimpleResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping("/me")
public class MeController {
    @Resource
    private EmailServiceImpl emailService;

    @Resource
    private UserServiceImpl userService;


    @GetMapping
    public String web() {
        return "me";
    }

    @GetMapping("/get")
    @ResponseBody
    public SimpleResponse<Object> getInfo(){
        return SimpleResponse.ok("成功返回", userService.getAuthenticationInfo());
    }

    @GetMapping("/sendVerifyCode")
    @ResponseBody
    public SimpleResponse<Object> sendVerifyCode() {
        User user = userService.getAuthentication();
        // 无法获取认证用户时
        if (user == null){
            return SimpleResponse.fail("用户异常");
        }
        if(emailService.sendVerifyCode("INFO_CHANGE_VERIFY", user.getEmail())){
            return SimpleResponse.ok("验证码已发送至您的邮箱，请注意查收");
        }
        return SimpleResponse.fail("验证码发送失败，请稍后再试");
    }

    @PostMapping("/requestChange")
    @ResponseBody
    public SimpleResponse<Object> requestPasswordChange(@RequestParam String name, @RequestParam String password,
                                                        @RequestParam String email, @RequestParam String code) {
        // 获取认证用户
        User user = userService.getAuthentication();
        // 再次验证用户是否存在
        if(user == null){
            return SimpleResponse.fail("用户异常");
        }
        // 验证邮箱与验证码
        if(!emailService.checkVerifyCode("INFO_CHANGE_VERIFY", user.getEmail(), code)){
            return SimpleResponse.fail("邮箱验证码无效");
        }
        // 尝试更新用户
        try{
            userService.update(user.getId(), name, password, email, user.getDepartmentId());
        }catch(UserException e){
            return SimpleResponse.fail(e.getMessage());
        }
        // 返回成功提示
        return SimpleResponse.ok("信息修改成功");
    }
}
