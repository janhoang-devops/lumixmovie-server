package com.project.lumix.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id
    private String name;
    private String description;
}
