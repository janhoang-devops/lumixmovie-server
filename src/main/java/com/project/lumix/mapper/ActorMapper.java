package com.project.lumix.mapper;

import com.project.lumix.dto.request.ActorRequest;
import com.project.lumix.dto.response.ActorResponse;
import com.project.lumix.entity.Actor;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring"
)
public interface ActorMapper {
    Actor toActor(ActorRequest request);

    ActorResponse toActorResponse(Actor actor);
}