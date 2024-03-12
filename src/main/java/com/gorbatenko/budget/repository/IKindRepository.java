package com.gorbatenko.budget.repository;

import com.gorbatenko.budget.model.Kind;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IKindRepository extends JpaRepository<Kind, UUID> {
    @Transactional
    @Modifying
    void deleteByUserGroupAndId(UUID userGroup, UUID id);

    Kind findByUserGroupAndId(UUID userGroup, UUID id);

    List<Kind> findAllByUserGroupOrderByTypeAscNameAsc(UUID userGroup);

    @Query(value = "select k.*  from kinds k  where k.user_group = :userGroup and k.type = :type and k.name = :name",
            nativeQuery = true)
    Optional<Kind> findByUserGroupAndTypeAndName(UUID userGroup, String type, String name);

    @Query(value = "select k.*  from kinds k  where k.user_group = :userGroup and k.type = :type and k.hidden = false order by k.name",
            nativeQuery = true)
    List<Kind> findByUserGroupAndType(UUID userGroup, String type);
}
