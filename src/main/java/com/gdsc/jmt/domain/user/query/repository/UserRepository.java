package com.gdsc.jmt.domain.user.query.repository;

import com.gdsc.jmt.domain.user.query.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserAggregateId(String id);

    Optional<UserEntity> findByEmail(String email);
}
