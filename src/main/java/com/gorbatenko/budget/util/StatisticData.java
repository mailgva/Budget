package com.gorbatenko.budget.util;

import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.doc.User;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StatisticData {
    LocalDate startDate;
    LocalDate endDate;
    LocalDateTime offSetStartDate;
    LocalDateTime offSetEndDate;
    List<User> users;
    List<BudgetItem> listBudgetItems;
}
