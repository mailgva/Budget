package com.gorbatenko.budget.util;

import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.User;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class StatisticData {
    LocalDate startDate;
    LocalDate endDate;
    List<User> users;
    List<BudgetItem> listBudgetItems;
}
