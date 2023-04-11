package com.gorbatenko.budget.config.regularoperation;


import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.model.doc.User;
import com.gorbatenko.budget.repository.BudgetItemRepository;
import com.gorbatenko.budget.repository.RegularOperationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Configuration
@EnableScheduling
public class RegularOperationConfig {
    private RegularOperationRepository regularOperationRepository;

    private BudgetItemRepository budgetItemRepository;

    public RegularOperationConfig(RegularOperationRepository regularOperationRepository, BudgetItemRepository budgetItemRepository) {
        this.regularOperationRepository = regularOperationRepository;
        this.budgetItemRepository = budgetItemRepository;
    }

    @Value("${app.regularoperation.enabled}")
    private boolean enabled;

    @Scheduled(cron = "${app.regularoperation.cron.expression:-}")
    public void startAddOperation() {
        if (! enabled) return;

        List<RegularOperation> operations = regularOperationRepository.adminGetAll();
        operations.forEach(operation -> {
            boolean execute;

            switch (operation.getEvery()) {
                case DEFINITE_DAY_OF_MONTH :
                    execute = (operation.getDayOfMonth() == LocalDate.now().getDayOfMonth());
                    break;
                case DAY:
                    execute = true;
                    break;
                default:
                    execute = operation.getEvery().name().equals(LocalDate.now().getDayOfWeek().name()) ;
                    break;
            }

            if (execute) {
                budgetItemRepository.adminSave(createFromOperation(operation));
            }
        });
    }

    private BudgetItem createFromOperation(RegularOperation operation) {
        BudgetItem budgetItem = new BudgetItem(new User(operation.getUser().getId(), operation.getUser().getName()),
                operation.getKind(),
                LocalDateTime.of(LocalDate.now(), LocalTime.MIN),
                operation.getDescription(),
                operation.getPrice(),
                operation.getCurrency());

        budgetItem.setCreateDateTime(LocalDateTime.now().plusMinutes(operation.getCountUserTimezomeOffsetMinutes()));
        budgetItem.setUserGroup(operation.getUserGroup());
        return budgetItem;
    }
}
