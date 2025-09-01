package com.example.user.application.mapper;

import com.example.user.domain.model.Role;
import com.example.user.domain.model.User;
import com.example.user.application.dto.UserResponseDto;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
  UserResponseDto toDto(User user);

  default Set<String> mapRoles(Set<Role> roles) {
    return roles == null ? java.util.Set.of()
      : roles.stream().map(Role::getName).collect(Collectors.toSet());
  }
}
