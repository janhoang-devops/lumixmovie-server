package com.project.lumix.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchHistory {
    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private String id;
    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;
    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "movie_id",
            nullable = false
    )
    private Movie movie;
    @Column(
            name = "progress_in_seconds"
    )
    private int progressInSeconds;
    @UpdateTimestamp
    @Column(
            name = "last_watched_at"
    )
    private LocalDateTime lastWatchedAt;
    @Column(
            name = "is_finished",
            columnDefinition = "boolean default false"
    )
    private boolean isFinished;
}
