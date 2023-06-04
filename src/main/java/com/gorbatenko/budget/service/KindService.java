package com.gorbatenko.budget.service;

import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.repository.KindRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KindService {
    private KindRepository kindRepository;

    public KindService(KindRepository kindRepository) {
        this.kindRepository = kindRepository;
    }

    public Kind save(Kind kind) {
        return kindRepository.save(kind);
    }

    public Kind getById(String id) {
        return kindRepository.getById(id);
    }

    public List<Kind> getAll() {
        return kindRepository.getAll();
    }

    public void deleteById(String id) {
        kindRepository.deleteById(id);
    }

    public List<Kind> getKindsByNameAndType(String name, Type type) {
        return kindRepository.getFilteredData(null, name, type, null);
    }

    public List<Kind> getKindsByType(Type type) {
        return kindRepository.getFilteredData(null, null, type, null);
    }
}
