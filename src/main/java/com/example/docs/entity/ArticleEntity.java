package com.example.docs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@Table(name = "articles")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // название

    @Column(name = "journal_name")
    private String journalName; // название журнала

    @Column(name = "number_of_journal")
    private String numberOfJournal; // номер журнала

    @Column(name = "public_date")
    private LocalDate publicDate; // публикация журнала

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project; // проект

    @OneToMany(mappedBy = "article")
    @ToString.Exclude
    private List<CommentEntity> comments; // комментарии

    @ManyToMany
    @JoinTable(name = "articles_authors",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"article_id", "author_id"})
    )
    @ToString.Exclude
    private List<UserEntity> authors; // авторы статьи

    @ManyToMany
    @JoinTable(name = "articles_liked_users",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"article_id", "user_id"})
    )
    @ToString.Exclude
    private List<UserEntity> likedUsers; // лайки

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ArticleEntity that = (ArticleEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
