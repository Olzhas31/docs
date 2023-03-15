package com.example.docs.repository;

import com.example.docs.entity.LaboratoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaboratoryEntityRepository extends JpaRepository<LaboratoryEntity, Long> {
}