package com.nikhil.multitenant.controller;

import com.nikhil.multitenant.context.TenantContext;
import com.nikhil.multitenant.dto.ProjectResponseDto;
import com.nikhil.multitenant.model.Project;
import com.nikhil.multitenant.repository.ProjectRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @GetMapping
    public ResponseEntity<ProjectResponseDto> getProjects() {
        UUID tenantId = TenantContext.getTenantId();
        List<Project> projects = projectRepository.findAllByTenant_Id(tenantId);
        ProjectResponseDto projectResponseDto = new ProjectResponseDto();
        projectResponseDto.setProjects(projects);
        return ResponseEntity.ok(projectResponseDto);
    }
}
