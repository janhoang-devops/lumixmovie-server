package com.project.lumix.mapper;

import com.project.lumix.dto.request.CommentRequest;
import com.project.lumix.dto.response.CommentResponse;
import com.project.lumix.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    Comment toComment(CommentRequest commentRequest);

    @Mappings({
            @Mapping(source = "user.username", target = "username"),
            @Mapping(source = "user.userId", target = "userId"),
            @Mapping(source = "user.email", target = "email"),
            @Mapping(source = "movie.id", target = "movieId"),
            @Mapping(source = "movie.title", target = "movieTitle"),
            @Mapping(source = "movie.posterUrl", target = "posterUrl"),
            @Mapping(source = "movie.year", target = "year"),
            @Mapping(source = "parent.id", target = "parentId"),
            @Mapping(source = "parent.user.username", target = "parentName"),
            @Mapping(target = "replies", expression = "java(mapReplies(comment.getReplies()))")
    })
    CommentResponse toCommentResponse(Comment comment);

    @Mappings({
            @Mapping(target = "user", ignore = true),
            @Mapping(target = "movie", ignore = true),
            @Mapping(target = "parent", ignore = true)
    })
    void updateComment(@MappingTarget Comment comment, CommentRequest request);

    List<CommentResponse> toResponseList(List<Comment> comments);

    // ✅ Map replies 1 cấp (tránh vòng lặp)
    default List<CommentResponse> mapReplies(List<Comment> replies) {
        if (replies == null) return null;
        return replies.stream()
                .map(reply -> CommentResponse.builder()
                        .id(reply.getId())
                        .content(reply.getContent())
                        .createdAt(reply.getCreatedAt())
                        .updatedAt(reply.getUpdatedAt())
                        .username(reply.getUser().getUsername())
                        .userId(reply.getUser().getUserId())
                        .email(reply.getUser().getEmail())
                        .parentId(reply.getParent() != null ? reply.getParent().getId() : null)
                        .parentName(reply.getParent() != null ? reply.getParent().getUser().getUsername() : null)
                        .build())
                .toList();
    }
}
