package com.liliya.contract.web.contract;

import com.liliya.contract.model.exception.ContractException;
import com.liliya.contract.model.response.LayuiFormDataResponse;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping("/contract/approval")
public class ContractApprovalController {
    @Resource
    private ContractServiceImpl contractService;

    @Resource
    private ContractApprovalServiceImpl contractApprovalService;

    @GetMapping
    public String web() {
        return "approval-contract";
    }

    @PostMapping("/getPage")
    @ResponseBody
    public LayuiFormDataResponse<Object> getPage(@RequestParam Integer page, @RequestParam Integer limit,
                                         @RequestParam(required = false) Integer type, @RequestParam(required = false) String name,
                                         @RequestParam(required = false) String author, @RequestParam(required = false) String party) {
        // 返回信息
        return contractService.getPage(1, page, limit, type, null, name, author, party);
    }

    @PostMapping("/submit")
    @ResponseBody
    public SimpleResponse<Object> submit(@RequestParam Integer id){
        // 提交合同审批
        try {
            contractApprovalService.ApprovalSubmit(id);
            return SimpleResponse.ok("成功提交合同审批");
        } catch (ContractException e) {
            return SimpleResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/cancel")
    @ResponseBody
    public SimpleResponse<Object> cancel(@RequestParam Integer id){
        // 撤回合同审批
        try {
            contractApprovalService.ApprovalCancel(id);
            return SimpleResponse.ok("成功撤回合同审批");
        } catch (ContractException e) {
            return SimpleResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/allow")
    @ResponseBody
    public SimpleResponse<Object> allow(@RequestParam Integer id, @RequestParam String note){
        // 同意合同审批
        try {
            contractApprovalService.ApprovalAllow(id, note);
            return SimpleResponse.ok("已同意该合同审批");
        } catch (ContractException e) {
            return SimpleResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/reject")
    @ResponseBody
    public SimpleResponse<Object> reject(@RequestParam Integer id){
        // 拒绝合同审批
        try {
            contractApprovalService.ApprovalReject(id);
            return SimpleResponse.ok("已拒绝该合同审批");
        } catch (ContractException e) {
            return SimpleResponse.fail(e.getMessage());
        }
    }
}
