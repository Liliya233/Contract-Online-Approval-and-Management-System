package com.liliya.contract.web.department;

import com.liliya.contract.model.exception.DepartmentException;
import com.liliya.contract.model.response.LayuiFormDataResponse;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.DepartmentServiceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping("/department")
@PreAuthorize("hasAnyRole('ADMIN')")
public class DepartmentController {
    @Resource
    private DepartmentServiceImpl departmentService;

    @GetMapping
    public String web() {
        return "management-department";
    }



    @PostMapping("/add")
    @ResponseBody
    public SimpleResponse<Object> add(@RequestParam String name, @RequestParam(required = false) String admin,
                              @RequestParam(required = false) String create, @RequestParam Integer approval,
                              @RequestParam(required = false) String seal, @RequestParam(required = false) String terminate,
                              @RequestParam Integer access) {
        // 尝试新增部门
        try{
            departmentService.add(name, admin, create, approval, seal, terminate, access);
        }catch (DepartmentException e) {
            return SimpleResponse.fail(e.getMessage());
        }
        return SimpleResponse.ok("部门新增成功");
    }

    @PostMapping("/edit")
    @ResponseBody
    public SimpleResponse<Object> edit(@RequestParam Integer id, @RequestParam String name,
                               @RequestParam(required = false) String admin, @RequestParam(required = false) String create,
                               @RequestParam Integer approval, @RequestParam(required = false) String seal,
                               @RequestParam(required = false) String terminate, @RequestParam Integer access) {
        // 尝试更新部门
        try{
            departmentService.update(id, name, admin, create, approval, seal, terminate, access);
        }catch (DepartmentException e) {
            return SimpleResponse.fail(e.getMessage());
        }
        return SimpleResponse.ok("部门更新成功");
    }

    @PostMapping("/delete")
    @ResponseBody
    public SimpleResponse<Object> delete(@RequestParam Integer id) {
        // 尝试删除部门
        try{
            departmentService.delete(id);
        }catch (DepartmentException e) {
            return SimpleResponse.fail(e.getMessage());
        }
        return SimpleResponse.ok("部门删除成功");
    }

    @PostMapping("/getPage")
    @ResponseBody
    public LayuiFormDataResponse<Object> getPage(@RequestParam Integer page, @RequestParam Integer limit,
                                                 @RequestParam(required = false, defaultValue = "0") Integer searchType,
                                                 @RequestParam(required = false) String searchText) {
        // 返回数据
        return departmentService.getPage(page, limit, searchType, searchText);
    }
}
