package com.taskapi.taskapi.mapper;

import com.taskapi.taskapi.dto.auth.UserResponse;
import com.taskapi.taskapi.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()))")
    UserResponse toResponse(User user);
}
