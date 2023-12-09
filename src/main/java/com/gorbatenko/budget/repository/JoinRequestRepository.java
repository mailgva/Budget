package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.JoinRequest;
import com.gorbatenko.budget.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class JoinRequestRepository extends AbstractRepository {

    private MongoTemplate mongoRepository;

    private IJoinRequestRepository repository;

    @Autowired
    public void setMongoRepository(MongoTemplate mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Autowired
    public void setRepository(IJoinRequestRepository repository) {
        this.repository = repository;
    }

    public JoinRequest save(JoinRequest joinRequest) {
        if (joinRequest.getUser() == null) {
            joinRequest.setUser(SecurityUtil.get().getUser());
        }
        joinRequest.setCreated(LocalDateTime.now());
        return repository.save(joinRequest);
    }

    public List<JoinRequest> getNewJoinRequests() {
        Criteria criteria = new Criteria();
        criteria.and("userGroup").is(SecurityUtil.get().getUser().getId());
        criteria.and("accepted").is(null);
        criteria.and("declined").is(null);
        Sort sort = Sort.by(Sort.Direction.ASC, "created");
        Query query = new Query(criteria);
        if (sort != null) {
            query.with(sort);
        }
        return mongoRepository.find(query, JoinRequest.class);
    }

    public JoinRequest findById(String id) {
        return mongoRepository.findById(id, JoinRequest.class);
    }

    public boolean isExistsNoAnsweredRequest(String userGroup) {
        Criteria criteria = new Criteria();
        criteria.and("user.id").is(SecurityUtil.get().getUser().getId());
        criteria.and("userGroup").is(userGroup);
        criteria.and("accepted").is(null);
        criteria.and("declined").is(null);
        Query query = new Query(criteria);
        return mongoRepository.find(query, JoinRequest.class).size() > 0;
    }

}
