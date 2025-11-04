package com.project.lumix.specification;

import com.project.lumix.dto.request.CommentSearchRequest;
import com.project.lumix.entity.Comment;
import com.project.lumix.entity.Movie;
import com.project.lumix.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CommentSpecification {

    public static Specification<Comment> fromRequest(CommentSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo nội dung bình luận
            if (StringUtils.hasText(request.getContent())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("content")),
                        "%" + request.getContent().toLowerCase() + "%"
                ));
            }

            // Lọc theo username của người dùng
            if (StringUtils.hasText(request.getUsername())) {
                Join<Comment, User> userJoin = root.join("user");
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("username")),
                        "%" + request.getUsername().toLowerCase() + "%"
                ));
            }

            // Lọc theo tên phim
            if (StringUtils.hasText(request.getMovieName())) {
                Join<Comment, Movie> movieJoin = root.join("movie");
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(movieJoin.get("title")),
                        "%" + request.getMovieName().toLowerCase() + "%"
                ));
            }

            // Lọc theo ngày bắt đầu
            if (request.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), request.getStartDate()
                ));
            }

            // Lọc theo ngày kết thúc
            if (request.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), request.getEndDate()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
