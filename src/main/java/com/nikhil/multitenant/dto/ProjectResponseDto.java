package com.nikhil.multitenant.dto;

import com.nikhil.multitenant.model.Project;
import lombok.Data;

import java.util.List;

@Data
public class ProjectResponseDto {
    private List<Project> projects;
}
