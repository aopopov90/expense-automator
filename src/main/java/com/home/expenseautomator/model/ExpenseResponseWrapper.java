package com.home.expenseautomator.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
public class ExpenseResponseWrapper {

    List<ExpenseResponse> expenses;
    JsonNode errors;
}
