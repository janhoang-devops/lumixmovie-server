package com.project.lumix.dto.request;
import lombok.Data;

@Data
public class MovieSearchRequest {
    private String title;
    private String genre;
    private Integer startYear;
    private Integer endYear;
    private String actorName;
}
