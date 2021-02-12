package com.home.expenseautomator.model;

import lombok.Data;

import java.util.List;

@Data
public class ExpenseResponseWrapper {

    List<ExpenseResponse> expenses;
}
