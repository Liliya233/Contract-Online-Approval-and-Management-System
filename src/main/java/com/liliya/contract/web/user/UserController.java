package com.liliya.contract.web.user;

import com.liliya.contract.model.exception.UserException;
import com.liliya.contract.service.impl.UserServiceImpl;
import com.liliya.contract.model.response.LayuiFormDataResponse;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.DepartmentServiceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasAnyRole('ADMIN')")
public class UserController {
    @Resource
    private UserServiceImpl userService;

    @Resource
    private DepartmentServiceImpl departmentService;

    @GetMapping
    public String web(Model model) {
        // 用于渲染部门选择列表
        model.addAttribute("departments", departmentService.getAll());
        return "management-user";
    }

    @PostMapping("/add")
    @ResponseBody
    public SimpleResponse<Object> add(@RequestParam String name, @RequestParam String password,
                                      @RequestParam String email, @RequestParam Integer departmentId) {
        // 尝试新增用户
        try{
            userService.add(name, password, email, departmentId);
        }catch (UserException e) {
            return SimpleResponse.fail(e.getMessage());
        }
        return SimpleResponse.ok("用户新增成功");
    }

    @PostMapping("/delete")
    @ResponseBody
    public SimpleResponse<Object> delete(@RequestParam Integer id) {
        // 尝试删除用户
        try{
            userService.delete(id);
        }catch (UserException e) {
            return SimpleResponse.fail(e.getMessage());
        }
        return SimpleResponse.ok("用户删除成功");
    }

    @PostMapping("/edit")
    @ResponseBody
    public SimpleResponse<Object> edit(@RequestParam Integer id, @RequestParam String name,
                                   @RequestParam(required = false) String password,
                                   @RequestParam String email, @RequestParam Integer departmentId) {
        // 尝试更新用户
        try{
            userService.update(id, name, password, email, departmentId);
        }catch (UserException e) {
            return SimpleResponse.fail(e.getMessage());
        }
        return SimpleResponse.ok("用户更新成功");
    }

    @PostMapping("/getPage")
    @ResponseBody
    public LayuiFormDataResponse<Object> getPage(@RequestParam Integer page, @RequestParam Integer limit,
                                                 @RequestParam(required = false, defaultValue = "0") Integer searchType,
                                                 @RequestParam(required = false) String searchText) {
        // 返回数据
        return userService.getPage(page, limit, searchType, searchText);
    }
}
