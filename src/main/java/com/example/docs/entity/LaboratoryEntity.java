package com.example.docs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Table(name = "laboratories")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LaboratoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // название лаборатории

    @Column(columnDefinition = "TEXT")
    private String description; // описание лаборатории

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "supervisor_id", referencedColumnName = "id")
    private UserEntity supervisor; // заведующии

    @OneToMany(mappedBy = "laboratory")
    @ToString.Exclude
    private List<UserDetailEntity> persons; // персонал


}
