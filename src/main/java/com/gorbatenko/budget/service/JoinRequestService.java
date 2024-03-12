package com.gorbatenko.budget.service;

import com.gorbatenko.budget.model.JoinRequest;
import com.gorbatenko.budget.repository.JoinRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JoinRequestService {
    private final JoinRequestRepository joinRequestRepository;

    public JoinRequest save(JoinRequest joinRequest) {
        return joinRequestRepository.save(joinRequest);
    }

    public JoinRequest getById(UUID id) {
        return joinRequestRepository.findById(id);
    }

    public boolean isExistsNoAnsweredRequest(UUID groupId) {
        return joinRequestRepository.isExistsNoAnsweredRequest(groupId);
    }

    public List<JoinRequest> getNewJoinRequests() {
        return joinRequestRepository.getNewJoinRequests();
    }

}
