package com.project.lumix.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;
import lombok.Data;

@Data
public class MovieCreateRequest {
    private @NotBlank(
            message = "Title is mandatory"
    ) String title;
    private @NotBlank(
            message = "POSTER_URL_NOT_BLANK"
    ) String videoUrl;
    private @NotBlank(
            message = "VIDEO_URL_NOT_BLANK"
    ) String posterUrl;
    @Column(
            columnDefinition = "TEXT"
    )
    private String description;
    private Set<String> genres;
    private String rating;
    private String year;
    private Set<String> directors;
    private Set<String> actors;
    private String duration;
    private String country;
    private @NotNull(message = "Release date is mandatory")
    LocalDate releaseDate;
}