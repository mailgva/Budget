package com.gorbatenko.budget.service;

import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.repository.RegularOperationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegularOperationService {
    private RegularOperationRepository regularOperationRepository;

    public RegularOperationService(RegularOperationRepository regularOperationRepository) {
        this.regularOperationRepository = regularOperationRepository;
    }

    public RegularOperation save(RegularOperation regularOperation) {
        return regularOperationRepository.save(regularOperation);
    }

    public RegularOperation getById(String id) {
        return regularOperationRepository.getById(id);
    }

    public void deleteById(String id) {
        regularOperationRepository.deleteById(id);;
    }

    public List<RegularOperation> getByCurrencyId(String id) {
        return regularOperationRepository.getByCurrencyId(id);
    }

    public List<RegularOperation> getByKindId(String id) {
        return regularOperationRepository.getByKindId(id);
    }

    public List<RegularOperation> getAll() {
        return regularOperationRepository.getAll();
    }
}
