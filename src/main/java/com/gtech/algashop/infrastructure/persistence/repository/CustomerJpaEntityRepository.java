package com.gtech.algashop.infrastructure.persistence.repository;

import com.gtech.algashop.infrastructure.persistence.entity.CustomerPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerJpaEntityRepository extends JpaRepository<CustomerPersistenceEntity, UUID> {
}
