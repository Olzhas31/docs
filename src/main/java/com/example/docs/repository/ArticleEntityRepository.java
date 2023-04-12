package com.example.docs.repository;

import com.example.docs.entity.ArticleEntity;
import com.example.docs.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleEntityRepository extends JpaRepository<ArticleEntity, Long> {

    List<ArticleEntity> findByAuthorsContains(UserEntity user);

}