package com.liliya.contract.service;

import com.liliya.contract.model.domain.ContractAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContractAttachmentService {
    // 新增附件记录
    ContractAttachment add(ContractAttachment attachment);

    // 删除附件
    ContractAttachment delete(Integer id);

    // 通过 ID 获取附件
    ContractAttachment getById(Integer id);

    // 根据合同 ID 获取附件
    List<ContractAttachment> getByContractId(Integer contractId, Integer type);

    // 新增合同文件 (单个)
    ContractAttachment addContract(String fileName, byte[] fileContent, Integer contractId);

    // 新增合同附件
    List<ContractAttachment> addAttachments(List<MultipartFile> files, Integer contractId);

    // 新增合同扫描件 (单个)
    ContractAttachment addScan(String fileName, byte[] fileContent, Integer contractId);

    // 新增合同终结附件
    List<ContractAttachment> addEndAttachments(List<MultipartFile> files, Integer contractId);
}
