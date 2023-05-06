package com.liliya.contract.web.supplement;

import com.liliya.contract.model.define.Status;
import com.liliya.contract.model.domain.Contract;
import com.liliya.contract.model.domain.Supplement;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.ContractServiceImpl;
import com.liliya.contract.service.impl.SupplementAttachmentServiceImpl;
import com.liliya.contract.service.impl.SupplementServiceImpl;
import com.liliya.contract.utils.WordUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/supplement/generate")
public class SupplementGenerateController {
    @Resource
    private SupplementServiceImpl supplementService;

    @Resource
    private ContractServiceImpl contractService;

    @Resource
    private WordUtils wordUtils;

    @Resource
    private SupplementAttachmentServiceImpl supplementAttachmentService;

    @PostMapping
    public SimpleResponse<Object> generate(@RequestParam(required = false) Integer templateId, @RequestParam Integer id,
                                           @RequestParam String name, @RequestParam String content){
        // 获取当前协议
        Supplement supplement = supplementService.getById(id);
        // 合法性检查
        if (!supplementService.hasAccessPermission(supplement)){
            return SimpleResponse.fail("无权访问");
        }
        // 仅允许未审核与审核不通过的协议
        Integer status = supplement.getStatus();
        if (!status.equals(Status.APPROVAL_AWAIT) && !status.equals(Status.APPROVAL_FAIL)){
            return SimpleResponse.fail("当前合同不允许生成协议文件");
        }
        // 读取模板文件
        // TODO: 根据模板ID读取对应模板
        byte[] bytes;
        try(FileInputStream fileInputStream= new FileInputStream("/contract/测试协议模板.docx")){
            bytes = fileInputStream.readAllBytes();
        }catch (Exception e){
            return SimpleResponse.fail("协议生成失败");
        }
        // 打包传入参数
        HashMap<String, String> map = new HashMap<>();
        Contract contract = contractService.getById(supplement.getContractId());
        {
            map.put("${name}", name);
            map.put("${party}", contract.getParty());
            map.put("${createTime}", contract.getCreateTime().toString());
            map.put("${contractName}", contract.getName());
            map.put("${content}", content);
        }
        // 根据字段与模板生成 DOCX 协议
        bytes = wordUtils.docxTemplateToDocx(bytes, map);
        // 转换为PDF
        bytes = wordUtils.docxToPDF(bytes);
        // 转换结果判断
        if (bytes == null){
            return SimpleResponse.fail("协议生成失败");
        }
        // 文件存储
        if (supplementAttachmentService.addSupplement("生成协议.pdf", bytes, id) != null){
            return SimpleResponse.ok("协议已成功生成并保存", Map.of(
                    "file", "data:application/pdf;base64,"+ Base64.getEncoder().encodeToString(bytes)
            ));
        }
        return SimpleResponse.fail("协议生成失败");
    }
}
