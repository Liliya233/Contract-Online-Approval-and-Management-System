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
@RequestMapping("/supplement/seal")
@PreAuthorize("hasAnyRole('SEAL')")
public class SupplementSealController {
    @Resource
    private SupplementServiceImpl supplementService;

    @Resource
    private SupplementApprovalServiceImpl supplementApprovalService;


    @GetMapping
    public String web() {
        return "seal-supplement";
    }

    @PostMapping("/getPage")
    @ResponseBody
    public LayuiFormDataResponse<Object> getPage(@RequestParam Integer page, @RequestParam Integer limit,
                                                 @RequestParam(required = false) String name,
                                                 @RequestParam(required = false) Integer contractId) {
        // 返回信息
        return supplementService.getPage(2, page, limit, null, name, contractId);
    }

    @PostMapping("/allow")
    @ResponseBody
    public SimpleResponse<Object> allow(@RequestParam Integer id, @RequestParam String note){
        try {
            supplementApprovalService.Seal(id, note);
            return SimpleResponse.ok("已成功为当前协议盖章");
        }catch (SupplementException e){
            return SimpleResponse.fail(e.getMessage());
        }
    }
}
