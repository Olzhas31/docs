package com.example.docs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@Table(name = "\"users\"")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;
    private String password;
    private String role;
    private Boolean enabled;
    private Boolean locked;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    private UserDetailEntity userDetail;

    @ManyToMany(mappedBy = "authors")
    @ToString.Exclude
    private List<ArticleEntity> articles;

    @ManyToMany(mappedBy = "likedUsers")
    @ToString.Exclude
    private List<ArticleEntity> likedArticles;

    @OneToMany(mappedBy = "author")
    @ToString.Exclude
    private List<DocumentEntity> documentsCretedByMe; // документы созданные мной

    @OneToMany(mappedBy = "executor")
    @ToString.Exclude
    private List<DocumentEntity> executorDocuments; // документы для исполнение

    @OneToOne(mappedBy = "supervisor", cascade = CascadeType.ALL)
    @ToString.Exclude
    private LaboratoryEntity laboratory;

    @ManyToMany(mappedBy = "users")
    @ToString.Exclude
    private List<ProjectEntity> projects;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        return Collections.singleton(authority);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserEntity that = (UserEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
