package com.project.lumix.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private String id;
    @Column(
            unique = true,
            nullable = false
    )
    private String name;
    public Genre(String name) {
        this.name = name;
    }
}
