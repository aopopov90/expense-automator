package com.home.expenseautomator.service;

import com.home.expenseautomator.config.Properties;
import com.home.expenseautomator.model.Expense;
import com.home.expenseautomator.model.ExpenseResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpenseService {

    private final WebClient swWebClient;
    private final Properties properties;

    public void submitExpense(Expense expense) {
        if (!expenseExists(expense)) {
            ExpenseResponseWrapper response = swWebClient.post()
                    .uri("/create_expense")
                    .body(Mono.just(expense), Expense.class)
                    .retrieve()
                    .onStatus(httpStatus -> !HttpStatus.OK.equals(httpStatus),
                            r -> r.bodyToMono(String.class).map(body -> new Exception(body)))
                    .bodyToMono(ExpenseResponseWrapper.class)
                    .block();

            if (response.getErrors().size() > 0)
                log.error("Failed to create expense: {}", response.getErrors());
            else
                log.info("Expense submitted: {}", expense.toString());
        } else {
            log.info("Expense already exists on Splitwise: {}", expense.toString());
        }

    }

    private boolean expenseExists(Expense expense) {
        return getExpenses().block()
                .getExpenses()
                .stream()
                .filter(expenseResponse -> Objects.isNull(expenseResponse.getDeletedAt()))
                .filter(expenseResponse -> expense.getDescription().equals(expenseResponse.getDescription()))
                .collect(Collectors.toList())
                .size() >= 1;
    }

    public Mono<ExpenseResponseWrapper> getExpenses() {
        return swWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/get_expenses")
                        .queryParam("dated_after",
                                Instant.now().minus(properties.getExpensesSpanDays(), ChronoUnit.DAYS).toString())
                        .queryParam("limit", properties.getExpensesLimit())
                        .build())
                .retrieve()
                .bodyToMono(ExpenseResponseWrapper.class);
    }
}
