package com.liliya.contract.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liliya.contract.dao.ContractAttachmentMapper;
import com.liliya.contract.model.define.FileType;
import com.liliya.contract.model.domain.ContractAttachment;
import com.liliya.contract.service.ContractAttachmentService;
import com.liliya.contract.utils.FileUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ContractAttachmentServiceImpl implements ContractAttachmentService {
    @Resource
    private ContractAttachmentMapper contractAttachmentMapper;

    @Resource
    private FileUtils fileUtils;

    @Override
    @CachePut(cacheNames = "contract-attachment-id", key = "#attachment.getId()", unless = "#result == null")
    public ContractAttachment add(ContractAttachment attachment) {
        // 设置上传时间
        attachment.setUploadTime(new Date(System.currentTimeMillis()));
        // 插入
        if(contractAttachmentMapper.insert(attachment) > 0){
            return attachment;
        }
        return null;
    }

    @Override
    @CacheEvict(cacheNames = "contract-attachment-id", key = "#id")
    public ContractAttachment delete(Integer id) {
        ContractAttachment attachment = ((ContractAttachmentServiceImpl) AopContext.currentProxy()).getById(id);
        // 删除文件
        fileUtils.delete(attachment.getUrl());
        // 删除附件对象
        if (contractAttachmentMapper.deleteById(attachment) > 0){
            return attachment;
        }
        return null;
    }

    @Override
    @Cacheable(cacheNames = "contract-attachment-id", key = "#id", unless = "#result == null")
    public ContractAttachment getById(Integer id) {
        return contractAttachmentMapper.selectById(id);
    }

    @Override
    public List<ContractAttachment> getByContractId(Integer contractId, Integer type) {
        return contractAttachmentMapper.selectList(
                new QueryWrapper<ContractAttachment>()
                        .eq("attachment_contract_id", contractId)
                        .eq("attachment_type", type)
        );
    }

    @Override
    @CachePut(cacheNames = "contract-attachment-id", key = "#result.getId()", unless = "#result == null")
    public ContractAttachment addContract(String fileName, byte[] fileContent, Integer contractId) {
        List<ContractAttachment> list = ((ContractAttachmentServiceImpl) AopContext.currentProxy()).getByContractId(contractId, FileType.FILE);
        // 删除已有的合同文件
        for (ContractAttachment attachment: list){
            ((ContractAttachmentServiceImpl) AopContext.currentProxy()).delete(attachment.getId());
        }
        // 新增合同文件
        String url = fileUtils.add(fileContent);
        if (url != null){
            // 新建附件对象
            ContractAttachment attachment = ContractAttachment
                    .builder()
                    .contractId(contractId)
                    .type(FileType.FILE)
                    .name(fileName)
                    .size((long) fileContent.length)
                    .url(url)
                    .build();
            // 新增至数据库
            return ((ContractAttachmentServiceImpl) AopContext.currentProxy()).add(attachment);
        }
        return null;
    }

    @Override
    public List<ContractAttachment> addAttachments(List<MultipartFile> files, Integer contractId) {
        List<ContractAttachment> result = new ArrayList<>();
        // 遍历合同附件
        for (MultipartFile file: files){
            // 新增合同附件
            String url = fileUtils.add(file);
            if (url != null){
                // 新建附件对象
                ContractAttachment attachment = ContractAttachment
                        .builder()
                        .contractId(contractId)
                        .type(FileType.ATTACHMENT)
                        .name(file.getOriginalFilename())
                        .size(file.getSize())
                        .url(url)
                        .build();
                // 新增至数据库
                if (((ContractAttachmentServiceImpl) AopContext.currentProxy()).add(attachment) != null){
                    // 成功则将此对象放进返回列表里
                    result.add(attachment);
                }
            }
        }
        return result;
    }

    @Override
    @CachePut(cacheNames = "contract-attachment-id", key = "#result.getId()", unless = "#result == null")
    public ContractAttachment addScan(String fileName, byte[] fileContent, Integer contractId) {
        List<ContractAttachment> list = ((ContractAttachmentServiceImpl) AopContext.currentProxy()).getByContractId(contractId, FileType.SCAN);
        // 删除已有的合同扫描件
        for (ContractAttachment attachment: list){
            ((ContractAttachmentServiceImpl) AopContext.currentProxy()).delete(attachment.getId());
        }
        // 新增合同扫描件
        String url = fileUtils.add(fileContent);
        if (url != null){
            // 新建附件对象
            ContractAttachment attachment = ContractAttachment
                    .builder()
                    .contractId(contractId)
                    .type(FileType.SCAN)
                    .name(fileName)
                    .size((long) fileContent.length)
                    .url(url)
                    .build();
            // 新增至数据库
            return ((ContractAttachmentServiceImpl) AopContext.currentProxy()).add(attachment);
        }
        return null;
    }

    @Override
    public List<ContractAttachment> addEndAttachments(List<MultipartFile> files, Integer contractId) {
        List<ContractAttachment> result = new ArrayList<>();
        // 遍历合同附件
        for (MultipartFile file: files){
            // 新增合同附件
            String url = fileUtils.add(file);
            if (url != null){
                // 新建附件对象
                ContractAttachment attachment = ContractAttachment
                        .builder()
                        .contractId(contractId)
                        .type(FileType.END_ATTACHMENT)
                        .name(file.getOriginalFilename())
                        .size(file.getSize())
                        .url(url)
                        .build();
                // 新增至数据库
                if (((ContractAttachmentServiceImpl) AopContext.currentProxy()).add(attachment) != null){
                    // 成功则将此对象放进返回列表里
                    result.add(attachment);
                }
            }
        }
        return result;
    }
}
