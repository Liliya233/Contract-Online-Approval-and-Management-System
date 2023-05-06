package com.liliya.contract.web.contract;

import com.liliya.contract.model.exception.ContractException;
import com.liliya.contract.model.response.LayuiFormDataResponse;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.ContractServiceImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

@Controller
@RequestMapping("/contract")
public class ContractBaseController {
    @Resource
    private ContractServiceImpl contractService;

    @GetMapping
    public String web() {
        return "management-contract";
    }

    @GetMapping("/details")
    public String detailsWeb() {
        return "contract-details";
    }

    @GetMapping("/edit")
    public String modifyWeb() {
        return "contract-edit";
    }

    @GetMapping("/scan")
    public String scanWeb() {
        return "contract-scan";
    }

    @GetMapping("/end")
    public String endWeb() {
        return "contract-end";
    }

    @GetMapping("/add")
    @ResponseBody
    @PreAuthorize("hasAnyRole('CREATE')")
    public SimpleResponse<Object> add(){
        // 尝试新增合同
        try {
            contractService.add();
        } catch (ContractException e) {
            return SimpleResponse.fail(e.getMessage());
        }
        return SimpleResponse.ok("新增合同成功，请前往管理界面进行编辑");
    }

    @PostMapping("/delete")
    @ResponseBody
    public SimpleResponse<Object> delete(@RequestParam Integer id) {
        // 尝试删除合同
        try {
            contractService.delete(id);
        } catch (ContractException e) {
            return SimpleResponse.fail(e.getMessage());
        }
        return SimpleResponse.ok("合同删除成功");
    }

    @PostMapping("/edit/submit")
    @ResponseBody
    public SimpleResponse<Object> edit(@RequestParam Integer id, @RequestParam String name, @RequestParam Integer type,
                                       @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                       @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate,
                                       @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date periodStartDate,
                                       @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date periodExpireDate,
                                       @RequestParam String party, @RequestParam Long funds) {
        // 尝试更新
        try{
            contractService.update(id, name, type, startDate, endDate, periodStartDate, periodExpireDate, party, funds);
        }catch (ContractException e){
            return SimpleResponse.fail(e.getMessage());
        }
        return SimpleResponse.ok("合同更新成功");
    }

    @PostMapping("/get")
    @ResponseBody
    public SimpleResponse<Object> get(@RequestParam Integer id){
        // 尝试获取信息
        try {
            return SimpleResponse.ok("成功返回", contractService.getInfo(id));
        }catch (ContractException e){
            return SimpleResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/getPage")
    @ResponseBody
    public LayuiFormDataResponse<Object> getPage(@RequestParam Integer page, @RequestParam Integer limit,
                                         @RequestParam(required = false) Integer type, @RequestParam(required = false) Integer status,
                                         @RequestParam(required = false) String name, @RequestParam(required = false) String author,
                                         @RequestParam(required = false) String party) {
        // 返回数据
        return contractService.getPage(0, page, limit, type, status, name, author, party);
    }
}
