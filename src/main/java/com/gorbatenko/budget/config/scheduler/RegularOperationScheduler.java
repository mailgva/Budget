package com.gorbatenko.budget.config.scheduler;


import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.repository.BudgetItemRepository;
import com.gorbatenko.budget.repository.RegularOperationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableScheduling
public class RegularOperationScheduler {
    private RegularOperationRepository regularOperationRepository;

    private BudgetItemRepository budgetItemRepository;

    public RegularOperationScheduler(RegularOperationRepository regularOperationRepository, BudgetItemRepository budgetItemRepository) {
        this.regularOperationRepository = regularOperationRepository;
        this.budgetItemRepository = budgetItemRepository;
    }

    @Value("${app.regularoperation.enabled}")
    private boolean enabled;

    @Scheduled(cron = "${app.regularoperation.cron.expression:-}")
    public void startAddOperation() {
        if (!enabled) {
            return;
        }

        List<RegularOperation> operations = regularOperationRepository.adminFindAll();
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
        return BudgetItem.builder()
                .user(operation.getUser())
                .userGroup(operation.getUserGroup())
                .kind(operation.getKind())
                .dateAt(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .description(operation.getDescription())
                .price(operation.getPrice())
                .currency(operation.getCurrency())
                .build();
    }
}
