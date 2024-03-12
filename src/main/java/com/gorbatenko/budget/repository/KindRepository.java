package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.gorbatenko.budget.util.SecurityUtil.getUserGroup;

@Repository
@RequiredArgsConstructor
public class KindRepository {
    private final IKindRepository repository;

    public Kind save(Kind kind) {
        if (kind.getUserGroup() == null) {
            kind.setUserGroup(getUserGroup());
        }
        return repository.save(kind);
    }

    public void deleteById(UUID id) {
        repository.deleteByUserGroupAndId(getUserGroup(), id);
    }

    public List<Kind> findAll() {
        return repository.findAllByUserGroupOrderByTypeAscNameAsc(getUserGroup());
    }

    public Kind findById(UUID id) {
        return repository.findByUserGroupAndId(getUserGroup(), id);
    }

    public Kind findByTypeAndName(Type type, String name) {
        return repository.findByUserGroupAndTypeAndName(getUserGroup(), type.name(), name).orElse(null);
    }

    public List<Kind> findByType(Type type) {
        return repository.findByUserGroupAndType(getUserGroup(), type.name());
    }
}
