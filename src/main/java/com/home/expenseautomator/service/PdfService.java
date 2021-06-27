package com.home.expenseautomator.service;

import lombok.Data;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

import static com.home.expenseautomator.util.StringUtils.extractRegex;

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
        return extractRegex(text, "Total paid £(.*?)\n");
    }

    public String extractSlotTime(String text) throws IOException {
        return extractRegex(text, "Slot time:(.*?),");
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
