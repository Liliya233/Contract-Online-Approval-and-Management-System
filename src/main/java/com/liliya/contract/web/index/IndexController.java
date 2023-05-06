package com.liliya.contract.web.index;

import com.liliya.contract.model.domain.User;
import com.liliya.contract.service.impl.UserServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;

@Controller
public class IndexController {
    @Resource
    private UserServiceImpl userService;

    @GetMapping
    public String web(Model model) {
        User user = userService.getAuthentication();
        // 将用户名传递给 model
        if (user != null){
            model.addAttribute("userName", user.getName());
        }
        return "index";
    }
}
