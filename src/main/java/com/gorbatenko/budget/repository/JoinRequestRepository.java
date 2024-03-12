package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.JoinRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.gorbatenko.budget.util.SecurityUtil.get;

@Repository
@RequiredArgsConstructor
public class JoinRequestRepository {
    private final IJoinRequestRepository repository;

    public JoinRequest save(JoinRequest joinRequest) {
        if (joinRequest.getUser() == null) {
            joinRequest.setUser(get().getUser());
        }
        joinRequest.setCreatedAt(LocalDateTime.now());
        return repository.save(joinRequest);
    }

    public List<JoinRequest> getNewJoinRequests() {
        return repository.findNewJoinRequests(get().getUser().getId());
    }

    public JoinRequest findById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public boolean isExistsNoAnsweredRequest(UUID userGroup) {
        return !repository.findNoAnsweredRequests(get().getUser().getId(), userGroup).isEmpty();
    }

}
