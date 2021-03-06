package com.home.expenseautomator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class Expense {

    String cost;
    String description;
    boolean payment;
    @JsonIgnore
    Date deliveryDate;
    @JsonProperty("users__0__user_id")
    Integer users0UserId;
    @JsonProperty("users__1__user_id")
    Integer users1UserId;
    @JsonProperty("users__1__paid_share")
    String users1PaidShare;
    @JsonProperty("users__1__owed_share")
    String users1OwedShare;
    @JsonProperty("users__0__paid_share")
    String users0PaidShare;
    @JsonProperty("users__0__owed_share")
    String users0OwedShare;

    public Expense(String cost, String description, Integer users0UserId, Integer users1UserId) throws ParseException {
        DecimalFormat df = new DecimalFormat("###.#");
        String totalPaidHalf = df.format(Double.parseDouble(cost) * 0.5);
        this.cost = df.format(Double.parseDouble(totalPaidHalf) * 2);
        this.users0PaidShare = this.cost;
        this.users0OwedShare = totalPaidHalf;
        this.users1PaidShare = "0.0";
        this.users1OwedShare = totalPaidHalf;
        this.description = description;
        this.users0UserId = users0UserId;
        this.users1UserId = users1UserId;
    }
}
