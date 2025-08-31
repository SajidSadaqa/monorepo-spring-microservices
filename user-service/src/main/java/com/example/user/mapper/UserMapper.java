package com.example.user.mapper;

import com.example.user.domain.Role;
import com.example.user.domain.User;
import com.example.user.dto.UserResponse;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
  UserResponse toDto(User user);

  default Set<String> mapRoles(Set<Role> roles) {
    return roles == null ? java.util.Set.of()
      : roles.stream().map(Role::getName).collect(Collectors.toSet());
  }
}
