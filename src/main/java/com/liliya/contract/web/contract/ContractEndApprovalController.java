package com.liliya.contract.web.contract;

import com.liliya.contract.model.exception.ContractException;
import com.liliya.contract.model.response.LayuiFormDataResponse;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.ContractApprovalServiceImpl;
import com.liliya.contract.service.impl.ContractServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping("/contract/end/approval")
public class ContractEndApprovalController {
    @Resource
    private ContractServiceImpl contractService;

    @Resource
    private ContractApprovalServiceImpl contractApprovalService;

    @GetMapping
    public String web() {
        return "end-approval-contract";
    }

    @PostMapping("/submit")
    @ResponseBody
    public SimpleResponse<Object> submit(@RequestParam Integer id, @RequestParam String note){
        try {
            // 尝试提交合同终结审批
            contractApprovalService.EndSubmit(id, note);
            return SimpleResponse.ok("已提交当前合同终结审批");
        }catch (ContractException e){
            return SimpleResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/getPage")
    @ResponseBody
    public LayuiFormDataResponse<Object> getPage(@RequestParam Integer page, @RequestParam Integer limit,
                                                 @RequestParam(required = false) Integer type, @RequestParam(required = false) String name,
                                                 @RequestParam(required = false) String author, @RequestParam(required = false) String party) {
        // 返回信息
        return contractService.getPage(4, page, limit, type, null, name, author, party);
    }

    @PostMapping("/allow")
    @ResponseBody
    public SimpleResponse<Object> allow(@RequestParam Integer id, @RequestParam String note){
        try {
            // 尝试同意合同终结审批
            contractApprovalService.EndAllow(id, note);
            return SimpleResponse.ok("已同意当前合同终结审批");
        }catch (ContractException e){
            return SimpleResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/reject")
    @ResponseBody
    public SimpleResponse<Object> reject(@RequestParam Integer id){
        try {
            // 尝试拒绝合同备案
            contractApprovalService.EndReject(id);
            return SimpleResponse.ok("已拒绝当前合同终结审批");
        }catch (ContractException e){
            return SimpleResponse.fail(e.getMessage());
        }
    }
}
