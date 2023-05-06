package com.liliya.contract.web.contract;

import com.liliya.contract.model.define.Status;
import com.liliya.contract.model.domain.Contract;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.ContractAttachmentServiceImpl;
import com.liliya.contract.service.impl.ContractServiceImpl;
import com.liliya.contract.utils.WordUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.util.*;

@RestController
@RequestMapping("/contract/generate")
public class ContractGenerateController {
    @Resource
    private ContractServiceImpl contractService;

    @Resource
    private WordUtils wordUtils;

    @Resource
    private ContractAttachmentServiceImpl contractAttachmentService;

    @PostMapping
    public SimpleResponse<Object> generate(@RequestParam(required = false) Integer templateId, @RequestParam Integer id, @RequestParam String name,
                                   @RequestParam Integer type, @RequestParam String startDate, @RequestParam String endDate,
                                   @RequestParam String periodStartDate, @RequestParam String periodExpireDate,
                                   @RequestParam String party, @RequestParam Long funds, @RequestParam String content){
        // 获取当前合同
        Contract contract = contractService.getById(id);
        // 合法性检查
        if (contract == null || !contractService.hasAccessPermission(contract)){
            return SimpleResponse.fail("无权访问");
        }
        // 仅允许未审核与审核不通过的合同
        Integer status = contract.getStatus();
        if (!status.equals(Status.APPROVAL_AWAIT) && !status.equals(Status.APPROVAL_FAIL)){
            return SimpleResponse.fail("当前合同不允许生成合同文件");
        }
        // 读取模板文件
        // TODO: 根据模板ID读取对应模板
        byte[] bytes;
        try(FileInputStream fileInputStream= new FileInputStream("/contract/测试合同模板.docx")){
            bytes = fileInputStream.readAllBytes();
        }catch (Exception e){
            return SimpleResponse.fail("合同生成失败");
        }
        // 合同类型名称列表
        List<String> typeList = List.of("买卖", "电、水、气、热力", "赠与", "借款", "租赁", "融资租赁",
                "承揽", "建设工程", "运输", "技术", "保管", "仓储", "委托", "行纪", "居间", "其他");
        // 打包传入参数
        HashMap<String, String> map = new HashMap<>();
        {
            map.put("${name}", name);
            map.put("${type}", type >= 0 && type < typeList.size() ? typeList.get(type) : "未知");
            map.put("${startDate}", startDate);
            map.put("${endDate}", endDate);
            map.put("${periodStartDate}", periodStartDate);
            map.put("${periodExpireDate}", periodExpireDate);
            map.put("${party}", party);
            map.put("${funds}", funds.toString());
            map.put("${content}", content);
        }
        // 根据字段与模板生成 DOCX 合同
        bytes = wordUtils.docxTemplateToDocx(bytes, map);
        // 转换为PDF
        bytes = wordUtils.docxToPDF(bytes);
        // 转换结果判断
        if (bytes == null){
            return SimpleResponse.fail("合同生成失败");
        }
        // 文件存储
        if (contractAttachmentService.addContract("生成合同.pdf", bytes, id) != null){
            return SimpleResponse.ok("合同已成功生成并保存", Map.of(
                    "file", "data:application/pdf;base64,"+ Base64.getEncoder().encodeToString(bytes)
            ));
        }
        return SimpleResponse.fail("合同生成失败");
    }
}
