package com.home.expenseautomator;

import com.home.expenseautomator.config.Properties;
import com.home.expenseautomator.model.Expense;
import com.home.expenseautomator.service.ExpenseService;
import com.home.expenseautomator.service.GmailAbelAndColeService;
import com.home.expenseautomator.service.GmailSainsburysService;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.home.expenseautomator.util.StringUtils.extractRegex;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
public class ExpenseAutomatorApplication implements CommandLineRunner {

    private final ConfigurableApplicationContext ctx;
    private final Properties properties;

    private final GmailSainsburysService gmailService;
    private final GmailAbelAndColeService abelAndColeService;
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
        processSainsburys();
        processAbelAndCole();
    }

    private void processAbelAndCole() {

        List<Expense> expenseRequests = new ArrayList<>();
        try {
            abelAndColeService.extractEmailBody(3).forEach(emailBody -> {
                try {
                    SimpleDateFormat formatReceipt = new SimpleDateFormat("dd MMMM yyyy");
                    SimpleDateFormat formatTarget = new SimpleDateFormat("dd-MMM");
                    String description = formatTarget.format(
                            formatReceipt.parse(extractRegex(emailBody, "Date: (.*?)at"))) + " | Abel & Cole";
                    expenseRequests.add(new Expense(extractRegex(emailBody, "Subtotal:   £(.*?)\r\n"),
                            description,
                            properties.getUser1Id(),
                            properties.getUser0Id()));
                } catch (IOException | ParseException e) {
                    log.error("Failed to extract total from the receipt", e);
                }

            });
        } catch (IOException | GeneralSecurityException e) {
            log.error("Failing to extract Abel & Cole receipts", e);
        }
        // Submitting splitwise expenses
        expenseRequests.forEach(expense -> expenseService.submitExpense(expense));
    }

    private void processSainsburys() {
        // Extracting receipts from gmail
        List<String> receiptNames = new ArrayList<>();
        try {
            receiptNames = gmailService.extractReceipts(1);
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
                SimpleDateFormat formatReceipt = new SimpleDateFormat("EEEE d MMMM yyyy");
                SimpleDateFormat formatTarget = new SimpleDateFormat("dd-MMM");
                String description = formatTarget.format(
                        formatReceipt.parse(
                                pdfService.getSlotDate().replaceAll("(?<=\\d)(st|nd|rd|th)", "")
                                        .trim())) + " | Sainsbury's";
                expenseRequests.add(new Expense(pdfService.getTotalPaid(),
                        description,
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
