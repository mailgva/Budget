package com.gorbatenko.budget.service;

import com.gorbatenko.budget.model.RegularOperation;
import com.gorbatenko.budget.repository.RegularOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegularOperationService {
    private final RegularOperationRepository regularOperationRepository;

    public RegularOperation save(RegularOperation regularOperation) {
        return regularOperationRepository.save(regularOperation);
    }

    public RegularOperation findById(UUID id) {
        return regularOperationRepository.findById(id);
    }

    public void deleteById(UUID id) {
        regularOperationRepository.deleteById(id);;
    }

    public List<RegularOperation> findByCurrencyId(UUID id) {
        return regularOperationRepository.findByCurrencyId(id);
    }

    public List<RegularOperation> findByKindId(UUID id) {
        return regularOperationRepository.findByKindId(id);
    }

    public List<RegularOperation> getAll() {
        return regularOperationRepository.findAll();
    }
}
