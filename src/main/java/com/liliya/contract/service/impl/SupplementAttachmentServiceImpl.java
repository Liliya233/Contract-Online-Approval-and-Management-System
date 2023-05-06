package com.liliya.contract.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liliya.contract.dao.SupplementAttachmentMapper;
import com.liliya.contract.model.domain.SupplementAttachment;
import com.liliya.contract.service.SupplementAttachmentService;
import com.liliya.contract.utils.FileUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class SupplementAttachmentServiceImpl implements SupplementAttachmentService {
    @Resource
    private SupplementAttachmentMapper supplementAttachmentMapper;

    @Resource
    private FileUtils fileUtils;

    @Override
    @CachePut(cacheNames = "supplement-attachment-id", key = "#attachment.getId()", unless = "#result == null")
    public SupplementAttachment add(SupplementAttachment attachment) {
        // 设置上传时间
        attachment.setUploadTime(new Date(System.currentTimeMillis()));
        // 插入
        if(supplementAttachmentMapper.insert(attachment) > 0){
            return attachment;
        }
        return null;
    }

    @Override
    @CacheEvict(cacheNames = "supplement-attachment-id", key = "#id")
    public SupplementAttachment delete(Integer id) {
        SupplementAttachment attachment = ((SupplementAttachmentServiceImpl) AopContext.currentProxy()).getById(id);
        // 删除文件
        fileUtils.delete(attachment.getUrl());
        // 删除附件对象
        if (supplementAttachmentMapper.deleteById(attachment) > 0){
            return attachment;
        }
        return null;
    }

    @Override
    @Cacheable(cacheNames = "supplement-attachment-id", key = "#id", unless = "#result == null")
    public SupplementAttachment getById(Integer id) {
        return supplementAttachmentMapper.selectById(id);
    }

    @Override
    public List<SupplementAttachment> getBySupplementId(Integer supplementId, Integer type) {
        return supplementAttachmentMapper.selectList(
                new QueryWrapper<SupplementAttachment>()
                        .eq("attachment_supplement_id", supplementId)
                        .eq("attachment_type", type)
        );
    }

    @Override
    public SupplementAttachment addSupplement(String fileName, byte[] fileContent, Integer supplementId) {
        List<SupplementAttachment> list = ((SupplementAttachmentServiceImpl) AopContext.currentProxy()).getBySupplementId(supplementId, 0);
        // 删除已有的协议文件
        for (SupplementAttachment attachment: list){
            ((SupplementAttachmentServiceImpl) AopContext.currentProxy()).delete(attachment.getId());
        }
        // 新增协议文件
        String url = fileUtils.add(fileContent);
        if (url != null){
            // 新建附件对象
            SupplementAttachment attachment = SupplementAttachment
                    .builder()
                    .supplementId(supplementId)
                    .type(0)
                    .name(fileName)
                    .size((long) fileContent.length)
                    .url(url)
                    .build();
            // 新增至数据库
            return ((SupplementAttachmentServiceImpl) AopContext.currentProxy()).add(attachment);
        }
        return null;
    }

    @Override
    public List<SupplementAttachment> addAttachments(List<MultipartFile> files, Integer supplementId) {
        List<SupplementAttachment> result = new ArrayList<>();
        // 遍历合同附件
        for (MultipartFile file: files){
            // 新增合同附件
            String url = fileUtils.add(file);
            if (url != null){
                // 新建附件对象
                SupplementAttachment attachment = SupplementAttachment
                        .builder()
                        .supplementId(supplementId)
                        .type(1)
                        .name(file.getOriginalFilename())
                        .size(file.getSize())
                        .url(url)
                        .build();
                // 新增至数据库
                if (((SupplementAttachmentServiceImpl) AopContext.currentProxy()).add(attachment) != null){
                    // 成功则将此对象放进返回列表里
                    result.add(attachment);
                }
            }
        }
        return result;
    }

    @Override
    public SupplementAttachment addScan(String fileName, byte[] fileContent, Integer supplementId) {
        List<SupplementAttachment> list = ((SupplementAttachmentServiceImpl) AopContext.currentProxy()).getBySupplementId(supplementId, 2);
        // 删除已有的协议文件
        for (SupplementAttachment attachment: list){
            ((SupplementAttachmentServiceImpl) AopContext.currentProxy()).delete(attachment.getId());
        }
        // 新增协议文件
        String url = fileUtils.add(fileContent);
        if (url != null){
            // 新建附件对象
            SupplementAttachment attachment = SupplementAttachment
                    .builder()
                    .supplementId(supplementId)
                    .type(2)
                    .name(fileName)
                    .size((long) fileContent.length)
                    .url(url)
                    .build();
            // 新增至数据库
            return ((SupplementAttachmentServiceImpl) AopContext.currentProxy()).add(attachment);
        }
        return null;
    }
}
