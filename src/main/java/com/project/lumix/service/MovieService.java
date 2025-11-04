package com.project.lumix.service;

import com.project.lumix.dto.request.GenreRequest;
import com.project.lumix.dto.request.MovieCreateRequest;
import com.project.lumix.dto.request.MovieSearchRequest;
import com.project.lumix.dto.request.MovieUpdateRequest;
import com.project.lumix.dto.response.ActorResponse;
import com.project.lumix.dto.response.DirectorResponse;
import com.project.lumix.dto.response.GenreResponse;
import com.project.lumix.dto.response.MovieDetailResponse;
import com.project.lumix.entity.Actor;
import com.project.lumix.entity.Director;
import com.project.lumix.entity.Genre;
import com.project.lumix.entity.Movie;
import com.project.lumix.exception.AppException;
import com.project.lumix.exception.ErrorCode;
import com.project.lumix.mapper.ActorMapper;
import com.project.lumix.mapper.DirectorMapper;
import com.project.lumix.mapper.GenreMapper;
import com.project.lumix.mapper.MovieMapper;
import com.project.lumix.repository.ActorRepository;
import com.project.lumix.repository.DirectorRepository;
import com.project.lumix.repository.GenreRepository;
import com.project.lumix.repository.MovieRepository;
import com.project.lumix.specification.MovieSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MovieService {

    private final DirectorRepository directorRepository;
    private final ActorRepository actorRepository;
    private final GenreRepository genreRepository;
    private final MovieMapper movieMapper;
    private final MovieRepository movieRepository;
    private final GenreMapper genreMapper;
    private final ActorMapper actorMapper;
    private final DirectorMapper directorMapper;

    // ================= CRUD =================

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public MovieDetailResponse create(MovieCreateRequest request) {
        log.info("Create movie request: {}", request.getTitle());

        if (movieRepository.existsByTitle(request.getTitle())) {
            throw new AppException(ErrorCode.MOVIE_EXISTED);
        }

        Movie movie = movieMapper.toMovie(request);

        movie.setPosterUrl(request.getPosterUrl());
        movie.setVideoUrl(request.getVideoUrl());
        movie.setGenres(findOrCreateEntities(
                request.getGenres(),
                genreRepository::findByNameIn,
                Genre::new,
                genreRepository::saveAll
        ));
        movie.setActors(findOrCreateEntities(
                request.getActors(),
                actorRepository::findByNameIn,
                Actor::new,
                actorRepository::saveAll
        ));
        movie.setDirectors(findOrCreateEntities(
                request.getDirectors(),
                directorRepository::findByNameIn,
                Director::new,
                directorRepository::saveAll
        ));

        movieRepository.save(movie);

        log.info("Movie created successfully with ID :{}", movie.getId());
        return movieMapper.toMovieDetailResponse(movie);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public MovieDetailResponse update(String id, MovieUpdateRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_EXISTED));

        if (!movie.getTitle().equals(request.getTitle())
                && movieRepository.existsByTitle(request.getTitle())) {
            throw new AppException(ErrorCode.MOVIE_EXISTED);
        }

        movieMapper.updateMovie(movie, request);

        movie.setPosterUrl(request.getPosterUrl());
        movie.setVideoUrl(request.getVideoUrl());
        movie.setGenres(findOrCreateEntities(
                request.getGenres(),
                genreRepository::findByNameIn,
                Genre::new,
                genreRepository::saveAll
        ));
        movie.setActors(findOrCreateEntities(
                request.getActors(),
                actorRepository::findByNameIn,
                Actor::new,
                actorRepository::saveAll
        ));
        movie.setDirectors(findOrCreateEntities(
                request.getDirector(),
                directorRepository::findByNameIn,
                Director::new,
                directorRepository::saveAll
        ));

        Movie updated = movieRepository.save(movie);
        return movieMapper.toMovieDetailResponse(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String id) {
        if (!movieRepository.existsById(id)) {
            throw new AppException(ErrorCode.MOVIE_NOT_EXISTED);
        }
        movieRepository.deleteById(id);
    }

    // ================= QUERY =================

    public List<MovieDetailResponse> getAllMovies() {
        log.info("Get all movies");
        return movieRepository.findAll()
                .stream()
                .map(movieMapper::toMovieDetailResponse)
                .toList();
    }

    public MovieDetailResponse getMovie(String id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_EXISTED));
        return movieMapper.toMovieDetailResponse(movie);
    }

    public Page<MovieDetailResponse> searchMovies(MovieSearchRequest request, Pageable pageable) {
        log.info("Searching for movies with criteria: {}", request);
        Specification<Movie> spec = MovieSpecification.fromRequest(request);
        return movieRepository.findAll(spec, pageable)
                .map(movieMapper::toMovieDetailResponse);
    }

    public List<MovieDetailResponse> getMoviesByGenres(String genreName) {
        log.info("Fetching movies for genre: {}", genreName);
        return movieRepository.findByGenres_Name(genreName)
                .stream()
                .map(movieMapper::toMovieDetailResponse)
                .toList();
    }

    public List<MovieDetailResponse> getMoviesByGenreNames(List<String> genreNames) {
        if (genreNames == null || genreNames.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Fetching movies for genres: {}", genreNames);
        return movieRepository.findByGenres_NameIn(genreNames)
                .stream()
                .map(movieMapper::toMovieDetailResponse)
                .toList();
    }

    public Optional<Movie> findMovieByTitle(String title) {
        log.info("Finding movie by title: {}", title);
        return movieRepository.findByTitle(title);
    }

    public List<MovieDetailResponse> getPopularMovies() {
        log.info("Get popular movies");
        return movieRepository.findTop20ByOrderByRatingDesc()
                .stream().map(movieMapper::toMovieDetailResponse)
                .toList();
    }

    public List<MovieDetailResponse> getTrendingMovies() {
        log.info("Get trending movies");
        return movieRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(movieMapper::toMovieDetailResponse)
                .toList();
    }

    public List<GenreResponse> getGenre() {
        log.info("Get genre request");
        return genreRepository.findAll()
                .stream()
                .map(genreMapper::toGenreResponse)
                .toList();
    }
    @PreAuthorize("hasRole('ADMIN')")
    public GenreResponse createGenre(GenreRequest request){
        log.info("Create genre request....");
        Genre genre = genreMapper.toGenre(request);
        genreRepository.save(genre);
        return genreMapper.toGenreResponse(genre);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public GenreResponse updateGenre(String id, GenreRequest request){
        Genre genre = genreRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.GENRE_NOT_FOUND));
        if(!genre.getName().equals(request.getName())
                && genreRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.GENRE_EXISTED);
        }
        genreMapper.updateGenre(genre, request);
        Genre updated = genreRepository.save(genre);
        return genreMapper.toGenreResponse(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteGenre(String id){
        if(!genreRepository.existsById(id)){
            throw new AppException(ErrorCode.GENRE_EXISTED);
        }
        genreRepository.deleteById(id);
    }
    public List<ActorResponse> getActor() {
        log.info("Get actor request");
        return actorRepository.findAll()
                .stream()
                .map(actorMapper::toActorResponse)
                .toList();
    }

    public List<DirectorResponse> getDirector() {
        log.info("Get director request");
        return directorRepository.findAll()
                .stream()
                .map(directorMapper::toDirectorResponse)
                .toList();
    }

    // ================= HELPER =================

    private <T> Set<T> findOrCreateEntities(
            Set<String> names,
            Function<Set<String>, List<T>> findByNames,
            Function<String, T> entityConstructor,
            Function<List<T>, List<T>> saveAll
    ) {
        if (names == null || names.isEmpty()) return Collections.emptySet();

        // 1. Lấy entity đã tồn tại
        List<T> existingEntities = findByNames.apply(names);

        // 2. Lấy danh sách tên đã có
        Set<String> existingNames = existingEntities.stream()
                .map(entity -> {
                    try {
                        return (String) entity.getClass().getMethod("getName").invoke(entity);
                    } catch (Exception e) {
                        throw new RuntimeException("Entity must have getName()", e);
                    }
                })
                .collect(Collectors.toSet());

        // 3. Tìm các tên chưa có
        Set<String> newNames = names.stream()
                .filter(name -> !existingNames.contains(name))
                .collect(Collectors.toSet());

        // 4. Tạo entity mới
        List<T> newEntities = newNames.stream()
                .map(entityConstructor)
                .toList();

        // 5. Lưu entity mới
        List<T> savedEntities = saveAll.apply(newEntities);

        // 6. Gộp kết quả
        existingEntities.addAll(savedEntities);
        return new HashSet<>(existingEntities);
    }
}
