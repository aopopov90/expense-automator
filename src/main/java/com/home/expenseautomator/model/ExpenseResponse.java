package com.home.expenseautomator.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ExpenseResponse {

    String description;

    @JsonProperty("deleted_at")
    String deletedAt;
}
