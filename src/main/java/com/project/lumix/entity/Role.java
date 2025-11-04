package com.project.lumix.entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    private String name;
    private String description;
    @ManyToMany
    private Set<Permission> permissions;
}
