package com.example.docs.repository;

import com.example.docs.entity.DocumentEntity;
import com.example.docs.entity.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentEntityRepository extends JpaRepository<DocumentEntity, Long> {

    List<DocumentEntity> findAllByStatusIn(List<String> statuses);

    List<DocumentEntity> findAllByType(String type);

    List<DocumentEntity> findAllByFolder(FolderEntity folder);
}