package com.microservices.user.application.mapper;

import com.microservices.user.application.dto.UserResponse;
import com.microservices.user.domain.entity.RoleEntity;
import com.microservices.user.domain.entity.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(target = "roles", expression = "java(mapRoles(userEntity.getRoleEntities()))")
  UserResponse toDto(UserEntity userEntity);

  default Set<String> mapRoles(Set<RoleEntity> roleEntities) {
    return roleEntities == null ? java.util.Set.of()
      : roleEntities.stream().map(RoleEntity::getName).collect(Collectors.toSet());
  }
}
