package com.project.lumix.mapper;

import com.project.lumix.dto.request.DirectorRequest;
import com.project.lumix.dto.response.DirectorResponse;
import com.project.lumix.entity.Director;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring"
)
public interface DirectorMapper {
    Director toDirector(DirectorRequest request);

    DirectorResponse toDirectorResponse(Director director);
}
