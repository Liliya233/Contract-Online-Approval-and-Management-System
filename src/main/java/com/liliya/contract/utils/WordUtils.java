package com.liliya.contract.utils;

import org.apache.poi.xwpf.usermodel.*;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Component
public class WordUtils {
    @Resource
    private DocumentConverter documentConverter;

    // 私有方法，替换段落内容
    private void replaceParagraph(XWPFParagraph paragraph, HashMap<String, String> replaceMap){
        List<XWPFRun> run = paragraph.getRuns();
        // 遍历段落文字对象
        for (XWPFRun xwpfRun: run){
            // 段落为空跳过
            if (xwpfRun == null){
                continue;
            }
            // 获取段落内容
            String content = xwpfRun.getText(xwpfRun.getTextPosition());
            // 内容为空跳过
            if (content == null || content.isEmpty()){
                continue;
            }
            // 关键字匹配
            for (String key: replaceMap.keySet()){
                // 内容替换
                content = content.replace(key, replaceMap.get(key));
            }
            // 写入替换内容
            xwpfRun.setText(content, 0);
        }
    }

    // BYTE: DOC => DOCX
    public byte[] docToDocx(byte[] doc) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(doc);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            // 文件转换
            documentConverter
                    .convert(byteArrayInputStream)
                    .as(DefaultDocumentFormatRegistry.DOC)
                    .to(byteArrayOutputStream)
                    .as(DefaultDocumentFormatRegistry.DOCX)
                    .execute();
            // 返回转换文件的 Bytes
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    // BYTE: DOCX 模板 => DOCX
    public byte[] docxTemplateToDocx(byte[] docxTemplate, HashMap<String, String> replaceMap) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(docxTemplate);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            // 读取模板文档
            XWPFDocument document = new XWPFDocument(byteArrayInputStream);
            // 替换段落中的指定文字
            Iterator<XWPFParagraph> itPara = document.getParagraphsIterator();
            while (itPara.hasNext()) {
                // 替换当前段落的文字信息
                replaceParagraph(itPara.next(), replaceMap);
            }
            // 替换表格中的指定文字
            // 获取文档中所有的表格，每个表格是一个元素
            Iterator<XWPFTable> itTable = document.getTablesIterator();
            while (itTable.hasNext()) {
                //获取表格内容
                XWPFTable table = itTable.next();
                // 表格行遍历
                for (XWPFTableRow xwpfTableRow: table.getRows()){
                    // 行单元格遍历
                    for (XWPFTableCell xwpfTableCell: xwpfTableRow.getTableCells()){
                        // 单元格段落遍历
                        for (XWPFParagraph xwpfParagraph: xwpfTableCell.getParagraphs()){
                            replaceParagraph(xwpfParagraph, replaceMap);
                        }
                    }
                }
            }
            document.write(byteArrayOutputStream);
            // 返回转换文件的 Bytes
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    // BYTE: DOCX => PDF
    public byte[] docxToPDF(byte[] docx) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(docx);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            // 文件转换
            documentConverter
                    .convert(byteArrayInputStream)
                    .as(DefaultDocumentFormatRegistry.DOCX)
                    .to(byteArrayOutputStream)
                    .as(DefaultDocumentFormatRegistry.PDF)
                    .execute();
            // 返回转换文件的 Bytes
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
