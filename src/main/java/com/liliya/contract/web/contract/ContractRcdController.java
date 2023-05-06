package com.liliya.contract.web.contract;

import com.liliya.contract.model.exception.ContractException;
import com.liliya.contract.model.response.LayuiFormDataResponse;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping("/contract/record")
@PreAuthorize("hasAnyRole('SEAL')")
public class ContractRcdController {
    @Resource
    private ContractServiceImpl contractService;

    @Resource
    private ContractApprovalServiceImpl contractApprovalService;

    @GetMapping
    public String web() {
        return "record-contract";
    }

    @PostMapping("/getPage")
    @ResponseBody
    public LayuiFormDataResponse<Object> getPage(@RequestParam Integer page, @RequestParam Integer limit,
                                         @RequestParam(required = false) Integer type, @RequestParam(required = false) String name,
                                         @RequestParam(required = false) String author, @RequestParam(required = false) String party) {
        // 返回信息
        return contractService.getPage(3, page, limit, type, null, name, author, party);
    }

    @PostMapping("/allow")
    @ResponseBody
    public SimpleResponse<Object> allow(@RequestParam Integer id, @RequestParam String note){
       try {
           // 尝试同意合同备案
           contractApprovalService.RecordAllow(id, note);
           return SimpleResponse.ok("已通过当前合同的备案申请");
       }catch (ContractException e){
           return SimpleResponse.fail(e.getMessage());
       }
    }

    @PostMapping("/reject")
    @ResponseBody
    public SimpleResponse<Object> reject(@RequestParam Integer id){
        try {
            // 尝试拒绝合同备案
            contractApprovalService.RecordReject(id);
            return SimpleResponse.ok("已拒绝当前合同的备案申请");
        }catch (ContractException e){
            return SimpleResponse.fail(e.getMessage());
        }
    }
}
