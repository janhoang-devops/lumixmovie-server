package com.project.lumix.controller;

import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.service.CommentReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentReactionController {
    private final CommentReactionService commentReactionService;

    @PostMapping("/{commentId}/like")
    public ApiResponse<Void> likeComment(@PathVariable String commentId){
        commentReactionService.toggleReaction(commentId,true);
        return ApiResponse.<Void>builder()
                .message("Liked comment successfully")
                .build();
    }

    @PostMapping("/{commentId}/dislike")
    public ApiResponse<Void> dislikeComment(@PathVariable String commentId){
        commentReactionService.toggleReaction(commentId,false);
        return ApiResponse.<Void>builder()
                .message("Disliked comment successfully")
                .build();
    }

    @GetMapping("/{commentId}/reactions/like")
    public ApiResponse<Long> getReactionLikeCounts(@PathVariable String commentId){
         long likes = commentReactionService.countLikes(commentId);

         return ApiResponse.<Long>builder()
                 .message("Reaction counts")
                 .result(likes)
                 .build();
    }
    @GetMapping("/{commentId}/reactions/dislike")
    public ApiResponse<Long> getReactionDislikeCounts(@PathVariable String commentId){
        long dislikes = commentReactionService.countDisLikes(commentId);

        return ApiResponse.<Long>builder()
                .message("Reaction counts")
                .result(dislikes)
                .build();
    }
}
