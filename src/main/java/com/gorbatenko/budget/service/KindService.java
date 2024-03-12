package com.gorbatenko.budget.service;

import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.repository.KindRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KindService {
    private final KindRepository kindRepository;

    public Kind save(Kind kind) {
        return kindRepository.save(kind);
    }

    public Kind findById(UUID id) {
        return kindRepository.findById(id);
    }

    public List<Kind> findAll() {
        return kindRepository.findAll();
    }

    public void deleteById(UUID id) {
        kindRepository.deleteById(id);
    }

    public Kind findByNameAndType(Type type, String name) {
        return kindRepository.findByTypeAndName(type, name);
    }

    public List<Kind> findByType(Type type) {
        return kindRepository.findByType(type);
    }
}
