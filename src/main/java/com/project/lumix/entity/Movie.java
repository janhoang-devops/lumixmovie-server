package com.project.lumix.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private String id;
    private String title;
    private String videoUrl;
    private String posterUrl;
    @Column(
            columnDefinition = "TEXT"
    )
    private String description;
    @ManyToMany
    private Set<Genre> genres;
    private String rating;
    private String year;
    @ManyToMany
    private Set<Director> directors;
    @ManyToMany
    private Set<Actor> actors;
    private String duration;
    private String country;
    @CreationTimestamp
    @Column(
            updatable = false
    )
    private LocalDateTime createdAt;
    @OneToMany(
            mappedBy = "movie",
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    @JsonManagedReference
    private Set<Comment> comments;
    @ManyToMany(
            mappedBy = "favoriteMovies"
    )
    @JsonIgnore
    private Set<User> favoritedByUsers;

}
