package com.project.lumix.mapper;

import com.project.lumix.dto.request.MovieCreateRequest;
import com.project.lumix.dto.request.MovieUpdateRequest;
import com.project.lumix.dto.response.MovieDetailResponse;
import com.project.lumix.entity.Actor;
import com.project.lumix.entity.Director;
import com.project.lumix.entity.Genre;
import com.project.lumix.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    // Mapping từ request sang entity
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "actors", ignore = true)
    @Mapping(target = "directors", ignore = true)
    Movie toMovie(MovieCreateRequest request);

    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "actors", ignore = true)
    @Mapping(target = "directors", ignore = true)
    void updateMovie(@MappingTarget Movie movie, MovieUpdateRequest request);

    // Mapping từ entity sang response
    @Mapping(target = "genres", source = "genres")
    @Mapping(target = "actors", source = "actors")
    @Mapping(target = "directors", source = "directors")
    MovieDetailResponse toMovieDetailResponse(Movie movie);

    default Set<String> mapGenres(Set<Genre> genres) {
        if (genres == null) return null;
        return genres.stream().map(Genre::getName).collect(Collectors.toSet());
    }

    default Set<String> mapActors(Set<Actor> actors) {
        if (actors == null) return null;
        return actors.stream().map(Actor::getName).collect(Collectors.toSet());
    }

    default Set<String> mapDirectors(Set<Director> directors) {
        if (directors == null) return null;
        return directors.stream().map(Director::getName).collect(Collectors.toSet());
    }
}
