package com.home.expenseautomator.service;

import com.home.expenseautomator.model.Expense;
import com.home.expenseautomator.model.ExpenseResponse;
import com.home.expenseautomator.model.ExpenseResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpenseService {

    private final WebClient swWebClient;

    public boolean expenseExists() {
        ExpenseResponseWrapper response = getExpenses().block();

        List<ExpenseResponse> expenseResponses = response
                .getExpenses()
                .stream()
                .filter(expenseResponse -> Objects.isNull(expenseResponse.getDeletedAt()))
                .collect(Collectors.toList());

        expenseResponses.forEach(expenseResponse -> log.info(expenseResponse.getDescription() +
                " | " + expenseResponse.getDeletedAt()));

        return false;
    }

    public void submitExpense(Expense expense) {
        String response = swWebClient.post()
                .uri("/create_expense")
                .body(Mono.just(expense), Expense.class)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println(expense);

    }

    public Mono<ExpenseResponseWrapper> getExpenses() {
        return swWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/get_expenses")
                        .queryParam("dated_after", "2021-02-01T00:00:00Z")
                        .build())
                .retrieve()
                .bodyToMono(ExpenseResponseWrapper.class);
    }
}
