package com.project.lumix.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private String id;
    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "movie_id"
    )
    @JsonBackReference
    private Movie movie;
    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "user_id"
    )
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference
    private Comment parent;

    @OneToMany(mappedBy = "parent",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<Comment> replies;
}