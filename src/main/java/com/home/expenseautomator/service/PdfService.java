package com.home.expenseautomator.service;

import lombok.Data;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Component
public class PdfService {

    private String totalPaid;
    private String slotDate;
    private String filePath;

    public void parse(String filePath) throws IOException {
        this.filePath = filePath;
        String text = parsePdf(this.filePath);
        this.totalPaid = extractTotalPaid(text);
        this.slotDate = extractSlotTime(text);
    }

    public String extractTotalPaid(String text) throws IOException {
        Pattern pattern = Pattern.compile("Total paid £(.*?)\n");
        Matcher matcher = pattern.matcher(text);
        String result = null;
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    public String extractSlotTime(String text) throws IOException {
        Pattern pattern = Pattern.compile("Slot time:(.*?),");
        Matcher matcher = pattern.matcher(text);
        String result = null;
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    private String parsePdf(String filePath) throws IOException {
        File file = new File(filePath);
        PDDocument pdDoc = PDDocument.load(file);
        String text = new PDFTextStripper().getText(pdDoc);
        pdDoc.close();
        return text;
    }

    public void deleteFile() {
        new File(this.filePath).delete();
    }
}
