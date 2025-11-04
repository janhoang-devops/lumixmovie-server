package com.project.lumix.dto.response;

import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDetailResponse {
    private String id;
    private String title;
    private String videoUrl;
    private String posterUrl;
    private String description;
    private Set<String> genres;
    private String rating;
    private String year;
    private Set<String> directors;
    private Set<String> actors;
    private String duration;
    private String country;
    private LocalDate releaseDate;
}
