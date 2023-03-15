package com.example.docs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@Table(name = "user_details")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String surname;
    private String middleName;
    private String address;
    private String phoneNumber;
    private String bio;
    private String position; // должность
    private LocalDate registerDate; // дата регистрации

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ToString.Exclude
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "laboratory_id")
    @ToString.Exclude
    private LaboratoryEntity laboratory; // лаборатория

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserDetailEntity that = (UserDetailEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
