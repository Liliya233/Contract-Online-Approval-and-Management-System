package com.liliya.contract.web.supplement;

import com.liliya.contract.model.exception.SupplementException;
import com.liliya.contract.model.response.LayuiFormDataResponse;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.SupplementApprovalServiceImpl;
import com.liliya.contract.service.impl.SupplementServiceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping("/supplement/record")
@PreAuthorize("hasAnyRole('SEAL')")
public class SupplementRcdController {
    @Resource
    private SupplementServiceImpl supplementService;

    @Resource
    private SupplementApprovalServiceImpl supplementApprovalService;

    @GetMapping
    public String web() {
        return "record-supplement";
    }

    @PostMapping("/getPage")
    @ResponseBody
    public LayuiFormDataResponse<Object> getPage(@RequestParam Integer page, @RequestParam Integer limit,
                                                 @RequestParam(required = false) String name,
                                                 @RequestParam(required = false) Integer contractId) {
        // 返回信息
        return supplementService.getPage(3, page, limit, null, name, contractId);
    }

    @PostMapping("/allow")
    @ResponseBody
    public SimpleResponse<Object> allow(@RequestParam Integer id, @RequestParam String note){
       try {
           // 尝试同意协议备案
           supplementApprovalService.RecordAllow(id, note);
           return SimpleResponse.ok("已通过当前协议的备案申请");
       }catch (SupplementException e){
           return SimpleResponse.fail(e.getMessage());
       }
    }

    @PostMapping("/reject")
    @ResponseBody
    public SimpleResponse<Object> reject(@RequestParam Integer id){
        try {
            // 尝试拒绝协议备案
            supplementApprovalService.RecordReject(id);
            return SimpleResponse.ok("已拒绝当前协议的备案申请");
        }catch (SupplementException e){
            return SimpleResponse.fail(e.getMessage());
        }
    }
}
