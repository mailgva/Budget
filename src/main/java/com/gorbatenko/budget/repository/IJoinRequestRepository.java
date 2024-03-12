package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IJoinRequestRepository extends JpaRepository<JoinRequest, UUID> {
    @Query(value = """
            select jr.* from join_requests jr
            where jr.user_group = :userGroup and jr.accepted_at is null and jr.declined_at is null
            order by created_at asc""", nativeQuery = true)
    List<JoinRequest> findNewJoinRequests(UUID userGroup);

    @Query(value = """
            select jr.* from join_requests jr
            where jr.user_id = :userId and jr.user_group = :userGroup
             and jr.accepted_at is null and jr.declined_at is null""", nativeQuery = true)
    List<JoinRequest> findNoAnsweredRequests(UUID userId, UUID userGroup);

}

