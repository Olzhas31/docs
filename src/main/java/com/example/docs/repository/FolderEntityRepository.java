package com.example.docs.repository;

import com.example.docs.entity.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderEntityRepository extends JpaRepository<FolderEntity, Long> {

    List<FolderEntity> findByIsMain(Boolean isMain);
}