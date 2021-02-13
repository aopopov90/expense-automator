package com.home.expenseautomator;

import com.home.expenseautomator.config.Properties;
import com.home.expenseautomator.model.Expense;
import com.home.expenseautomator.service.ExpenseService;
import com.home.expenseautomator.service.GmailService;
import com.home.expenseautomator.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
public class ExpenseAutomatorApplication implements CommandLineRunner {

    private final ConfigurableApplicationContext ctx;
    private final Properties properties;

    private final GmailService gmailService;
    private final ExpenseService expenseService;
    private final PdfService pdfService;

    public static void main(String[] args) {
        SpringApplication.run(ExpenseAutomatorApplication.class, args);
    }

    @Override
    public void run(String... args) {
        process();
        ctx.close();
    }

    private void process() {
        // Extracting receipts from gmail
        List<String> receiptNames = new ArrayList<>();
        try {
            receiptNames = gmailService.extractReceipts(3);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Failing to extract sainsbury's receipts", e);
        }

        // Build expense request objects
        List<Expense> expenseRequests = new ArrayList<>();
        receiptNames.forEach(receiptName -> {
            try {
                pdfService.parse(receiptName);
            } catch (IOException e) {
                log.error("Unable to parse pdf: {}", receiptName);
            }

            try {
                expenseRequests.add(new Expense(pdfService.getTotalPaid(),
                        pdfService.getSlotDate(),
                        properties.getUser0Id(),
                        properties.getUser1Id()));
                pdfService.deleteFile();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        // Submitting splitwise expenses
        expenseRequests.forEach(expense -> expenseService.submitExpense(expense));
    }

}
