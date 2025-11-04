package com.project.lumix.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {
    private String id;
    private String title;
    private String videoUrl;
    private String posterUrl;
    private String description;
    private String year;
    private String rating;
    private String createdAt;


}
