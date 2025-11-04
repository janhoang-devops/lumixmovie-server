package com.project.lumix.dto.request;

import jakarta.persistence.Column;

import java.time.LocalDate;
import java.util.Set;
import lombok.Data;

@Data
public class MovieUpdateRequest {
    private String title;
    private String videoUrl;
    private String posterUrl;
    @Column(
            columnDefinition = "TEXT"
    )
    private String description;
    private Set<String> genres;
    private String rating;
    private String year;
    private Set<String> director;
    private Set<String> actors;
    private String duration;
    private String country;
    private LocalDate releaseDate;
}
