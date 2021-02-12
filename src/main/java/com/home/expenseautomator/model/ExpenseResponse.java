package com.home.expenseautomator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExpenseResponse {
    String description;

    @JsonProperty("deleted_at")
    String deletedAt;
}
