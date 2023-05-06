package com.liliya.contract.web.supplement;

import com.liliya.contract.model.exception.SupplementException;
import com.liliya.contract.model.response.LayuiFormDataResponse;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.SupplementServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping("/supplement")
public class SupplementBaseController {
    @Resource
    private SupplementServiceImpl supplementService;

    @GetMapping
    public String web() {
        return "management-supplement";
    }

    @GetMapping("/details")
    public String detailsWeb() {
        return "supplement-details";
    }

    @GetMapping("/edit")
    public String modifyWeb() {
        return "supplement-edit";
    }

    @GetMapping("/scan")
    public String scanWeb() {
        return "supplement-scan";
    }

    @PostMapping("/add")
    @ResponseBody
    public SimpleResponse<Object> add(Integer contractId){
        // 尝试新增协议
        try {
            supplementService.add(contractId);
        } catch (SupplementException e) {
            return SimpleResponse.fail(e.getMessage());
        }
        return SimpleResponse.ok("新增补充协议成功，请前往管理界面进行编辑");
    }

    @PostMapping("/delete")
    @ResponseBody
    public SimpleResponse<Object> delete(@RequestParam Integer id) {
        // 尝试删除协议
        try {
            supplementService.delete(id);
        } catch (SupplementException e) {
            return SimpleResponse.fail(e.getMessage());
        }
        return SimpleResponse.ok("补充协议删除成功");
    }

    @PostMapping("/edit/submit")
    @ResponseBody
    public SimpleResponse<Object> edit(@RequestParam Integer id, @RequestParam String name) {
        // 尝试更新
        try{
            supplementService.update(id, name);
        }catch (SupplementException e){
            return SimpleResponse.fail(e.getMessage());
        }
        return SimpleResponse.ok("补充协议更新成功");
    }

    @PostMapping("/get")
    @ResponseBody
    public SimpleResponse<Object> get(@RequestParam Integer id){
        // 尝试获取信息
        try {
            return SimpleResponse.ok("成功返回", supplementService.getInfo(id));
        }catch (SupplementException e){
            return SimpleResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/getPage")
    @ResponseBody
    public LayuiFormDataResponse<Object> getPage(@RequestParam Integer page, @RequestParam Integer limit,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(required = false) String name,
                                         @RequestParam(required = false) Integer contractId) {
        // 返回数据
        return supplementService.getPage(0, page, limit, status, name, contractId);
    }
}
