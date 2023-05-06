package com.liliya.contract.utils;

import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Component
public class PDFUtils {
    @Value("${contract.waterMark}")
    private String waterMark;

    @Value("${contract.sealPath}")
    private String sealPath;

    // BYTE: PDF 添加水印
    public boolean addWaterMark(String pdfPath) {
        try (FileInputStream fileInputStream = new FileInputStream(pdfPath)){
            // 读取原文件
            byte[] srcPDF = fileInputStream.readAllBytes();
            fileInputStream.close();
            // 输出到原文件
            FileOutputStream fileOutputStream = new FileOutputStream(pdfPath);
            // 读取 PDF
            PdfStamper stamper =  PdfStamper.createXmlSignature(new PdfReader(srcPDF), fileOutputStream);
            // 水印字体，放到服务器上对应文件夹下（arial中文不生效）
            BaseFont base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
            // BaseFont font = BaseFont.CreateFont("/usr/share/fonts/simhei.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            // 文本设置
            PdfGState gs = new PdfGState();
            // 填充不透明度
            gs.setFillOpacity(0.1f);
            // 笔画不透明度
            gs.setStrokeOpacity(0.1f);
            // 用于水印的 Label
            JLabel label = new JLabel();
            label.setText(waterMark);
            // 获取文字方格的长宽
            FontMetrics metrics = label.getFontMetrics(label.getFont());
            int textH = metrics.getHeight();
            int textW = metrics.stringWidth(label.getText());
            // 遍历，为所有页面添加水印
            for (int i = 1; i < stamper.getReader().getNumberOfPages() + 1; i++) {
                // 获取当前页面参数
                Rectangle pageRect = stamper.getReader().getPageSize(i);
                // 在字体下方加水印
                PdfContentByte under = stamper.getOverContent(i);
                under.beginText();
                under.saveState();
                under.setGState(gs);
                // 设置水印字体与大小
                under.setFontAndSize(base, 15);
                // 设置字体上下间隔
                for (int height = textH; height < pageRect.getHeight(); height = height + textH * 9) {
                    // 设置字体左右间隔
                    for (int width = textW; width < pageRect.getWidth() + textW; width = width + textW * 2) {
                        // 设置倾斜角度
                        under.showTextAligned(Element.ALIGN_LEFT, waterMark, width - textW, height - textH, 45);
                    }
                }
                under.endText();
            }
            // 关闭 stamper
            stamper.flush();
            stamper.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // PDF 盖章
    public boolean addSeal(String pdfPath) {
        try(FileInputStream pdfInputStream = new FileInputStream(pdfPath);
            FileInputStream sealInputStream = new FileInputStream(sealPath)) {
            // 读取原文件
            byte[] srcPDF = pdfInputStream.readAllBytes();
            pdfInputStream.close();
            // 输出到原文件
            FileOutputStream fileOutputStream = new FileOutputStream(pdfPath);
            // 读取 PDF
            PdfStamper stamper =  PdfStamper.createXmlSignature(new PdfReader(srcPDF), fileOutputStream);
            // 图片读取
            Image img = Image.getInstance(sealInputStream.readAllBytes());
            // 设置图片大小
            img.scaleAbsolute(100, 100);
            // 设置图片位置
            img.setAbsolutePosition(400, 40);
            // 将图片放置的页码
            stamper.getOverContent(stamper.getReader().getNumberOfPages()).addImage(img);
            // 关闭表单编辑功能
            stamper.setFormFlattening(true);
            // 关闭 stamper
            stamper.flush();
            stamper.close();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
