package com.gorbatenko.budget.config.regularoperations;


import com.gorbatenko.budget.model.Budget;
import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.repository.BudgetRepository;
import com.gorbatenko.budget.repository.RegularOperationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@EnableScheduling
public class RegularOperationConfig {

    @Autowired
    private RegularOperationRepository regularOperationRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Value("${app.regularoperation.enabled}")
    private boolean enabled;

    @Scheduled(cron = "${app.regularoperation.cron.expression:-}")
    public void startAddOperation() {
        if (! enabled) return;

        List<RegularOperation> operations = regularOperationRepository.findAll();
        operations.forEach(operation -> {
            boolean execute = false;

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

            if (execute) budgetRepository.save(createFromOperation(operation));
        });
    }

    private Budget createFromOperation(RegularOperation operation) {
        Budget budget = new Budget(operation.getUser(),
                operation.getKind(),
                LocalDateTime.now(),
                operation.getDescription(),
                operation.getPrice(),
                operation.getCurrency());

        budget.setCreateDateTime(LocalDateTime.now().plusMinutes(operation.getCountUserTimezomeOffsetMinutes()));
        return budget;
    }
}
