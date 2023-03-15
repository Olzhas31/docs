package com.example.docs.entity;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Table(name = "comments")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content; // текст комментарии

    @Column(name = "created_time")
    private String createdTime; // время создание

    @ManyToOne
    @JoinColumn(name = "article_id")
    @ToString.Exclude
    private ArticleEntity article; // статья комментарии

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private UserEntity user; // автор комментарии

}
