package com.liliya.contract.service;

import com.liliya.contract.model.domain.SupplementAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SupplementAttachmentService {
    // 新增附件记录
    SupplementAttachment add(SupplementAttachment attachment);

    // 删除附件
    SupplementAttachment delete(Integer id);

    // 通过 ID 获取附件
    SupplementAttachment getById(Integer id);

    // 根据协议 ID 获取附件
    List<SupplementAttachment> getBySupplementId(Integer supplementId, Integer type);

    // 新增协议文件 (单个)
    SupplementAttachment addSupplement(String fileName, byte[] fileContent, Integer supplementId);

    // 新增协议附件
    List<SupplementAttachment> addAttachments(List<MultipartFile> files, Integer supplementId);

    // 新增协议扫描件 (单个)
    SupplementAttachment addScan(String fileName, byte[] fileContent, Integer supplementId);
}
