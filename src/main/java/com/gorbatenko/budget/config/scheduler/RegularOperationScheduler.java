package com.gorbatenko.budget.config.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.gorbatenko.budget.model.BudgetItem;
import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.repository.BudgetItemRepository;
import com.gorbatenko.budget.repository.RegularOperationRepository;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.regularoperation.enabled", havingValue = "true")
public class RegularOperationScheduler {
    private final RegularOperationRepository regularOperationRepository;
    private final BudgetItemRepository budgetItemRepository;

    public RegularOperationScheduler(RegularOperationRepository regularOperationRepository, BudgetItemRepository budgetItemRepository) {
        this.regularOperationRepository = regularOperationRepository;
        this.budgetItemRepository = budgetItemRepository;
    }

    @Scheduled(cron = "${app.regularoperation.cron.expression:-}")
    public void startAddOperation() {
        List<RegularOperation> operations = regularOperationRepository.adminFindAll();
        operations.forEach(operation -> {
            boolean execute = switch (operation.getEvery()) {
                case DEFINITE_DAY_OF_MONTH -> operation.getDayOfMonth() == LocalDate.now().getDayOfMonth();
                case DAY -> true;
                default -> operation.getEvery().name().equals(LocalDate.now().getDayOfWeek().name());
            };

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
