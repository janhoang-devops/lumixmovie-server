package com.project.lumix.entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvalidatedToken {
    @Id
    private String id;
    private Date expiryDate;
}
