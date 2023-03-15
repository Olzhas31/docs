package com.example.docs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@Table(name = "documents")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // Найменование
    private String type; // enum: тип документа
    private String folder; // enum: папка
    private String title; // тема
    private LocalDateTime createdTime; // время создание
    private LocalDateTime updatedTime; // время последнего обновление
    private LocalDateTime deadline; // срок
    private String number; // номер документа
    private String filename; // имя файлы, то что есть в системе
    private String status; // enum: статус документа

    @ManyToOne
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    private UserEntity author; // автор документа

    @ManyToOne
    @JoinColumn(name = "executor_id")
    @ToString.Exclude
    private UserEntity executor; // исполнитель

    @ManyToOne
    @JoinColumn(name = "approval_id")
    @ToString.Exclude
    private UserEntity approval; // утвердитель

    @ManyToMany
    @JoinTable(
            name = "documents_tags",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @ToString.Exclude
    private List<TagEntity> tags;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DocumentEntity that = (DocumentEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
