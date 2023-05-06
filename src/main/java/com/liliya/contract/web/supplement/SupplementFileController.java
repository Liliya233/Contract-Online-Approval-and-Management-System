package com.liliya.contract.web.supplement;

import com.liliya.contract.model.define.FileType;
import com.liliya.contract.model.define.Status;
import com.liliya.contract.model.domain.Supplement;
import com.liliya.contract.model.domain.SupplementAttachment;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.SupplementAttachmentServiceImpl;
import com.liliya.contract.service.impl.SupplementServiceImpl;
import com.liliya.contract.utils.FileUtils;
import com.liliya.contract.utils.WordUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/supplement")
public class SupplementFileController {
    @Resource
    private SupplementServiceImpl supplementService;

    @Resource
    private WordUtils wordUtils;

    @Resource
    private SupplementAttachmentServiceImpl supplementAttachmentService;

    @PostMapping("/upload/supplement")
    public SimpleResponse<Object> uploadSupplement(@RequestParam List<MultipartFile> file, @RequestParam Integer supplementId) {
        // 获取协议
        Supplement supplement = supplementService.getById(supplementId);
        // 权限检查，仅允许负责人上传文件
        if (!supplementService.isSelf(supplement)){
            return SimpleResponse.fail("无权访问");
        }
        // 仅允许未审核与审核不通过的协议
        Integer status = supplement.getStatus();
        if (!status.equals(Status.APPROVAL_AWAIT) && !status.equals(Status.APPROVAL_FAIL)){
            return SimpleResponse.fail("当前协议不允许上传协议文件");
        }
        // 仅允许上传单个协议文件
        if (file.size() != 1){
            return SimpleResponse.fail("仅允许上传单个协议文件");
        }
        // 取出文件对象
        MultipartFile fileObj = file.get(0);
        // 获得文件名
        String fileName = fileObj.getOriginalFilename();
        // 空文件名
        if (fileName == null || fileName.isEmpty()){
            return SimpleResponse.fail("无法处理该文件");
        }
        byte[] bytes;
        // 取出文件 Bytes
        try{
            bytes = fileObj.getBytes();
        }catch (Exception e){
            return SimpleResponse.fail("无法处理该文件");
        }
        // 文件转换
        if (fileName.endsWith(".doc")){
            bytes = wordUtils.docToDocx(bytes);
            fileName += ".docx";
        }
        if (fileName.endsWith(".docx")) {
            bytes = wordUtils.docxToPDF(bytes);
            fileName += ".pdf";
        }
        // 转换结果判断
        if (bytes == null){
            return SimpleResponse.fail("无法处理该文件");
        }
        // 文件存储
        if (supplementAttachmentService.addSupplement(fileName, bytes, supplementId) != null){
            return SimpleResponse.ok("协议文件已上传成功并保存", Map.of(
                    "file", "data:application/pdf;base64,"+ Base64.getEncoder().encodeToString(bytes)
            ));
        }
        return SimpleResponse.fail("协议文件上传失败");
    }

    @GetMapping("/download/supplement/{supplementId}")
    public ResponseEntity<byte[]> downloadSupplement(HttpServletRequest request, @PathVariable Integer supplementId) {
        // 获取协议
        Supplement supplement = supplementService.getById(supplementId);
        // 权限检查
        if (!supplementService.hasAccessPermission(supplement)){
            return new ResponseEntity<>("Permission denied".getBytes(), HttpStatus.BAD_REQUEST);
        }
        // 仅允许导出特定状态的协议文件
        Integer status = supplement.getStatus();
        if (!status.equals(Status.SEAL_AWAIT) && !status.equals(Status.SCAN_AWAIT) && !status.equals(Status.RECORD_FAIL)){
            return new ResponseEntity<>("Permission denied".getBytes(), HttpStatus.BAD_REQUEST);
        }
        // 获取附件对象
        SupplementAttachment attachment = supplementAttachmentService.getBySupplementId(supplementId, FileType.FILE).get(0);
        // 尝试返回
        try(InputStream is = new FileInputStream(attachment.getUrl())){
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", FileUtils.getFilename(request, attachment.getName()));
            //创建字节数组
            byte[] buffer = new byte[is.available()];
            //将流读到字节数组中
            is.read(buffer);
            return new ResponseEntity<>(buffer, headers, HttpStatus.OK);
        }catch (Exception e){
            // 下载失败提示
            return new ResponseEntity<>("Error Occurs".getBytes(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping("/upload/attachment")
    public SimpleResponse<Object> uploadAttachment(@RequestParam List<MultipartFile> file, @RequestParam Integer supplementId)  {
        // 获取协议
        Supplement supplement = supplementService.getById(supplementId);
        // 权限检查，仅允许负责人上传文件
        if (!supplementService.isSelf(supplement)){
            return SimpleResponse.fail("无权访问");
        }
        // 仅允许未审核与审核不通过的协议
        Integer status = supplement.getStatus();
        if (!status.equals(Status.APPROVAL_AWAIT) && !status.equals(Status.APPROVAL_FAIL)){
            return SimpleResponse.fail("当前协议不允许上传附件");
        }
        // 文件存储
        List<SupplementAttachment> attachments = supplementAttachmentService.addAttachments(file, supplementId);
        if (!attachments.isEmpty()){
            List<Integer> idList = new ArrayList<>();
            attachments.forEach(attachment -> idList.add(attachment.getId()));
            return SimpleResponse.ok("协议附件已上传成功并保存", Map.of(
                    "ids", idList
            ));
        }
        return SimpleResponse.fail("协议附件上传失败");
    }

    @PostMapping("/delete/attachment")
    public SimpleResponse<Object> deleteAttachment(@RequestParam Integer attachmentId) {
        // 获取附件对象
        SupplementAttachment attachment = supplementAttachmentService.getById(attachmentId);
        // 获取附件对应的协议对象
        Supplement supplement = supplementService.getById(attachment.getSupplementId());
        // 权限检查
        if (!supplementService.hasAccessPermission(supplement)){
            return SimpleResponse.fail("无权访问");
        }
        // 仅允许未审核与审核不通过的合同
        Integer status = supplement.getStatus();
        if (!status.equals(Status.APPROVAL_AWAIT) && !status.equals(Status.APPROVAL_FAIL)){
            return SimpleResponse.fail("当前协议不允许删除附件");
        }
        // 文件删除
        if (supplementAttachmentService.delete(attachmentId) != null){
            return SimpleResponse.ok("协议附件删除成功");
        }
        return SimpleResponse.fail("协议附件删除失败");
    }

    @GetMapping("/download/attachment/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(HttpServletRequest request, @PathVariable Integer attachmentId) {
        // 获取附件
        SupplementAttachment attachment = supplementAttachmentService.getById(attachmentId);
        // 获取协议
        Supplement supplement = supplementService.getById(attachment.getSupplementId());
        // 权限检查
        if (!supplementService.hasAccessPermission(supplement)){
            return new ResponseEntity<>("Permission denied".getBytes(), HttpStatus.BAD_REQUEST);
        }
        // 尝试返回
        try(InputStream is = new FileInputStream(attachment.getUrl())){
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", FileUtils.getFilename(request, attachment.getName()));
            //创建字节数组
            byte[] buffer = new byte[is.available()];
            //将流读到字节数组中
            is.read(buffer);
            return new ResponseEntity<>(buffer, headers, HttpStatus.OK);
        }catch (Exception e1){
            // 下载失败提示
            return new ResponseEntity<>("Error Occurs".getBytes(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping("/upload/scan")
    public SimpleResponse<Object> uploadScan(@RequestParam List<MultipartFile> file, @RequestParam Integer supplementId)  {
        // 获取协议
        Supplement supplement = supplementService.getById(supplementId);
        // 权限检查，仅允许负责人上传文件
        if (!supplementService.isSelf(supplement)){
            return SimpleResponse.fail("无权访问");
        }
        // 仅允许未审核与审核不通过的协议
        Integer status = supplement.getStatus();
        if (!status.equals(Status.SCAN_AWAIT) && !status.equals(Status.RECORD_FAIL)){
            return SimpleResponse.fail("当前协议不允许上传合同扫描版");
        }
        // 仅允许上传单个协议文件
        if (file.size() != 1){
            return SimpleResponse.fail("仅允许上传单个协议扫描版");
        }
        // 取出文件对象
        MultipartFile fileObj = file.get(0);
        // 获得文件名
        String fileName = fileObj.getOriginalFilename();
        // 空文件名
        if (fileName == null || fileName.isEmpty()){
            return SimpleResponse.fail("无法处理该文件");
        }
        byte[] bytes;
        // 取出文件 Bytes
        try{
            bytes = fileObj.getBytes();
        }catch (Exception e){
            return SimpleResponse.fail("无法处理该文件");
        }
        // 文件存储
        if (supplementAttachmentService.addScan(fileName, bytes, supplementId) != null){
            return SimpleResponse.ok("合同扫描版已上传成功并保存");
        }
        return SimpleResponse.fail("合同扫描版上传失败");
    }
}
