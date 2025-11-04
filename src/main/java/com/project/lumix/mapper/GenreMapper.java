package com.project.lumix.mapper;

import com.project.lumix.dto.request.GenreRequest;
import com.project.lumix.dto.response.GenreResponse;
import com.project.lumix.entity.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring"
)
public interface GenreMapper {
    Genre toGenre(GenreRequest request);

    GenreResponse toGenreResponse(Genre genre);

    void updateGenre(@MappingTarget Genre genre, GenreRequest request);
}
