package com.project.lumix.entity;
import com.project.lumix.enums.Provider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Setter
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private String userId;
    private String username;
    private String password;
    @Column(
            unique = true,
            nullable = false
    )
    private String email;
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @ManyToMany(
            fetch = FetchType.EAGER
    )
    @JoinTable(
            name = "user_roles",
            joinColumns = {@JoinColumn(
                    name = "user_id"
            )},
            inverseJoinColumns = {@JoinColumn(
                    name = "role_name"
            )}
    )
    private Set<Role> roles;
    @OneToMany(
            mappedBy = "user",
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    private Set<Comment> comments;
    @ManyToMany(
            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "user_favorites",
            joinColumns = {@JoinColumn(
                    name = "user_id"
            )},
            inverseJoinColumns = {@JoinColumn(
                    name = "movie_id"
            )}
    )
    private Set<Movie> favoriteMovies;
    private String verificationToken;
    private LocalDateTime tokenExpiryDate;
    @Column(
            columnDefinition = "boolean default false"
    )
    private boolean enabled;
    @Enumerated(EnumType.STRING)
    @Column(
            name = "provider",
            nullable = false
    )
    private Provider provider;
}
