package com.gorbatenko.budget.service;

import com.gorbatenko.budget.model.JoinRequest;
import com.gorbatenko.budget.repository.JoinRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JoinRequestService {
    private JoinRequestRepository joinRequestRepository;

    public JoinRequestService(JoinRequestRepository joinRequestRepository) {
        this.joinRequestRepository = joinRequestRepository;
    }

    public JoinRequest save(JoinRequest joinRequest) {
        return joinRequestRepository.save(joinRequest);
    }

    public JoinRequest getById(String id) {
        return joinRequestRepository.findById(id);
    }

    public boolean isExistsNoAnsweredRequest(String groupId) {
        return joinRequestRepository.isExistsNoAnsweredRequest(groupId);
    }

    public List<JoinRequest> getNewJoinRequests() {
        return joinRequestRepository.getNewJoinRequests();
    }

}
