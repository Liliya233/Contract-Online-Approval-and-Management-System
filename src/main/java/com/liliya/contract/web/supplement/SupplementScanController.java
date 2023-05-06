package com.liliya.contract.web.supplement;

import com.liliya.contract.model.exception.SupplementException;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.SupplementApprovalServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/supplement/scan")
public class SupplementScanController {
    @Resource
    private SupplementApprovalServiceImpl supplementApprovalService;


    @PostMapping("/submit")
    @ResponseBody
    public SimpleResponse<Object> submit(@RequestParam Integer id){
        try {
            // 尝试确认合同扫描版
            supplementApprovalService.ScanSubmit(id);
            return SimpleResponse.ok("已确认提交合同扫描版");
        }catch (SupplementException e){
            return SimpleResponse.fail(e.getMessage());
        }
    }
}
