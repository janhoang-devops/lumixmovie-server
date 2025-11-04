package com.project.lumix.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.lumix.entity.Actor;
import com.project.lumix.entity.Director;
import com.project.lumix.entity.Genre;
import com.project.lumix.entity.Movie;
import com.project.lumix.repository.ActorRepository;
import com.project.lumix.repository.DirectorRepository;
import com.project.lumix.repository.GenreRepository;
import com.project.lumix.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) {
        if (movieRepository.count() > 0) {
            log.info("Database already contains movie data. Skipping initialization.");
            return;
        }

        log.info("Starting data initialization from movies.json");
        try (InputStream inputStream = getClass().getResourceAsStream("/static/total/movies.json")) {
            if (inputStream == null) {
                log.error("Cannot find movies.json in classpath");
                return;
            }

            List<Map<String, Object>> movieDataList =
                    objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> movieData : movieDataList) {
                Movie movie = new Movie();
                movie.setTitle((String) movieData.get("title"));
                movie.setDescription((String) movieData.get("description"));
                movie.setYear((String) movieData.get("year"));
                movie.setRating((String) movieData.get("rating"));
                movie.setDuration((String) movieData.get("duration"));
                movie.setCountry((String) movieData.get("country"));
                movie.setPosterUrl((String) movieData.get("posterUrl"));
                movie.setVideoUrl((String) movieData.get("videoUrl"));

                // genres
                List<String> genreNames = (List<String>) movieData.get("genres");
                Set<Genre> genres = genreNames.stream()
                        .map(this::findOrCreateGenre)
                        .collect(Collectors.toSet());
                movie.setGenres(genres);

                // actors
                List<String> actorNames = (List<String>) movieData.get("actors");
                Set<Actor> actors = actorNames.stream()
                        .filter(name -> !name.equalsIgnoreCase("Diễn viên:"))
                        .map(this::findOrCreateActor)
                        .collect(Collectors.toSet());
                movie.setActors(actors);

                // directors
                List<String> directorNames = (List<String>) movieData.get("directors");
                Set<Director> directors = directorNames.stream()
                        .filter(name -> !name.equalsIgnoreCase("Đạo diễn:"))
                        .map(this::findOrCreateDirector)
                        .collect(Collectors.toSet());
                movie.setDirectors(directors);

                movieRepository.save(movie);
            }

            log.info("Successfully imported {} movies into the database.", movieDataList.size());
        } catch (Exception e) {
            log.error("Failed to read or import data from movies.json", e);
        }
    }

    private Genre findOrCreateGenre(String name) {
        return genreRepository.findByName(name)
                .orElseGet(() -> genreRepository.save(new Genre(null, name)));
    }

    private Actor findOrCreateActor(String name) {
        return actorRepository.findByName(name)
                .orElseGet(() -> actorRepository.save(new Actor(null, name)));
    }

    private Director findOrCreateDirector(String name) {
        return directorRepository.findByName(name)
                .orElseGet(() -> directorRepository.save(new Director(null, name)));
    }
}

