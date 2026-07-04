package com.nikhil.multitenant.dto;

import com.nikhil.multitenant.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SignupMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    User toEntity(SignupRequestDto signupRequestDto);
}
