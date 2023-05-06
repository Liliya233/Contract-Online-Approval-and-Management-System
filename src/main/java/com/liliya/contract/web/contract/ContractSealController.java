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
@RequestMapping("/contract/seal")
@PreAuthorize("hasAnyRole('SEAL')")
public class ContractSealController {
    @Resource
    private ContractServiceImpl contractService;

    @Resource
    private ContractApprovalServiceImpl contractApprovalService;


    @GetMapping
    public String web() {
        return "seal-contract";
    }

    @PostMapping("/getPage")
    @ResponseBody
    public LayuiFormDataResponse<Object> getPage(@RequestParam Integer page, @RequestParam Integer limit,
                                         @RequestParam(required = false) Integer type, @RequestParam(required = false) String name,
                                         @RequestParam(required = false) String author, @RequestParam(required = false) String party) {
        // 返回信息
        return contractService.getPage(2, page, limit, type, null, name, author, party);
    }

    @PostMapping("/allow")
    @ResponseBody
    public SimpleResponse<Object> allow(@RequestParam Integer id, @RequestParam String note){
        try {
            contractApprovalService.Seal(id, note);
            return SimpleResponse.ok("已成功为当前合同盖章");
        }catch (ContractException e){
            return SimpleResponse.fail(e.getMessage());
        }
    }
}
