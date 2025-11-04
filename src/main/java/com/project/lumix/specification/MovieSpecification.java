package com.project.lumix.specification;

import com.project.lumix.dto.request.MovieSearchRequest;
import com.project.lumix.entity.Genre;
import com.project.lumix.entity.Movie;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MovieSpecification {

    public static Specification<Movie> fromRequest(MovieSearchRequest request) {
        return (root,query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tìm theo tiêu đề (title)
            if (StringUtils.hasText(request.getTitle())) {
                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + request.getTitle().toLowerCase() + "%"
                );
                predicates.add(titlePredicate);
            }

            // Tìm theo thể loại (genre)
            if (StringUtils.hasText(request.getGenre())) {
                Join<Movie, Genre> genreJoin = root.join("genres"); // Movie.genres là ManyToMany
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(genreJoin.get("name")),
                        request.getGenre().toLowerCase()
                ));
            }

            // Tìm theo năm bắt đầu (>= startYear)
            if (request.getStartYear() != null) {
                Predicate yearPredicate = criteriaBuilder.greaterThanOrEqualTo(
                        root.get("year"), request.getStartYear()
                );
                predicates.add(yearPredicate);
            }

            // Tìm theo năm kết thúc (<= endYear)
            if (request.getEndYear() != null) {
                Predicate yearPredicate = criteriaBuilder.lessThanOrEqualTo(
                        root.get("year"), request.getEndYear()
                );
                predicates.add(yearPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

