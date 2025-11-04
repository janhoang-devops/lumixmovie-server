package com.project.lumix.controller;

import com.project.lumix.dto.request.CommentRequest;
import com.project.lumix.dto.response.CommentResponse;
import com.project.lumix.entity.Comment;
import com.project.lumix.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketCommentController {
    private final CommentService commentService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/comment/{movieId}")
    public void handleNewComment(@DestinationVariable String movieId, @Valid CommentRequest request){
        CommentResponse response = commentService.addCommentToMovie(movieId, request);

        messagingTemplate.convertAndSend("/topic/comments/" + movieId, response);
    }
}
