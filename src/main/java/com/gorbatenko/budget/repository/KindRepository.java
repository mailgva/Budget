package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.gorbatenko.budget.util.SecurityUtil.getUserGroup;

@Repository
public class KindRepository extends AbstractRepository {

    @Autowired
    private IKindRepository repository;

    public Kind save(Kind kind) {
        if (kind.getUserGroup() == null) {
            kind.setUserGroup(getUserGroup());
        }
        return repository.save(kind);
    }

    public void deleteById(String id) {
        repository.delete(this.getById(id));
    }

    public List<Kind> getAll() {
        Sort sort = Sort.by("type").ascending().and(Sort.by("name")).ascending();
        return findAll(null, sort, Kind.class);
    }

    public List<Kind> getFilteredData(String id, String name, Type type, Boolean hidden) {
        Criteria criteria = new Criteria();

        if (!isBlank(id)) {
            criteria.and("id").is(id);
        }
        if (type != null) {
            criteria.and("type").is(type);
        }
        if (!isBlank(name)) {
            criteria.and("name").is(name);
        }
        if (hidden != null) {
            if (hidden) {
                criteria.and("hidden").is(hidden);
            } else {
                criteria.orOperator(new Criteria().and("hidden").is(false), new Criteria().and("hidden").is(null));
            }
        }
        Sort sort = Sort.by("type").ascending().and(Sort.by("name")).ascending();
        return findAll(criteria, sort, Kind.class);
    }

    public Kind getById(String id) {
        return findById(id, Kind.class);
    }
}
