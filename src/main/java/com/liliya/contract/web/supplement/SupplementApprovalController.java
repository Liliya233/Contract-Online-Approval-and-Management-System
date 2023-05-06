package com.liliya.contract.web.supplement;

import com.liliya.contract.model.exception.SupplementException;
import com.liliya.contract.model.response.LayuiFormDataResponse;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.SupplementApprovalServiceImpl;
import com.liliya.contract.service.impl.SupplementServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping("/supplement/approval")
public class SupplementApprovalController {
    @Resource
    private SupplementServiceImpl supplementService;

    @Resource
    private SupplementApprovalServiceImpl supplementApprovalService;

    @GetMapping
    public String web() {
        return "approval-supplement";
    }

    @PostMapping("/getPage")
    @ResponseBody
    public LayuiFormDataResponse<Object> getPage(@RequestParam Integer page, @RequestParam Integer limit,
                                                 @RequestParam(required = false) String name,
                                                 @RequestParam(required = false) Integer contractId) {
        // 返回信息
        return supplementService.getPage(1, page, limit, null, name, contractId);
    }

    @PostMapping("/submit")
    @ResponseBody
    public SimpleResponse<Object> submit(@RequestParam Integer id){
        // 提交协议审批
        try {
            supplementApprovalService.ApprovalSubmit(id);
            return SimpleResponse.ok("成功提交补充协议审批");
        } catch (SupplementException e) {
            return SimpleResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/cancel")
    @ResponseBody
    public SimpleResponse<Object> cancel(@RequestParam Integer id){
        // 撤回协议审批
        try {
            supplementApprovalService.ApprovalCancel(id);
            return SimpleResponse.ok("成功撤回补充协议审批");
        } catch (SupplementException e) {
            return SimpleResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/allow")
    @ResponseBody
    public SimpleResponse<Object> allow(@RequestParam Integer id, @RequestParam String note){
        // 同意协议审批
        try {
            supplementApprovalService.ApprovalAllow(id, note);
            return SimpleResponse.ok("已同意该补充协议审批");
        } catch (SupplementException e) {
            return SimpleResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/reject")
    @ResponseBody
    public SimpleResponse<Object> reject(@RequestParam Integer id){
        // 拒绝协议审批
        try {
            supplementApprovalService.ApprovalReject(id);
            return SimpleResponse.ok("已拒绝该补充协议审批");
        } catch (SupplementException e) {
            return SimpleResponse.fail(e.getMessage());
        }
    }
}
