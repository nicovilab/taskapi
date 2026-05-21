package com.taskapi.taskapi.controller;

import com.taskapi.taskapi.dto.project.ProjectRequest;
import com.taskapi.taskapi.dto.project.ProjectResponse;
import com.taskapi.taskapi.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Project management")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Get all projects", description = "Returns projects belonging to the authenticated user")
    @ApiResponse(responseCode = "200", description = "List retrieved sucesfsully")
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> findAll(Authentication authentication){
        return ResponseEntity.ok(projectService.findAllByUser(authentication.getName()));
    }

    @Operation(summary = "Get project by ID")
    @ApiResponse(responseCode = "200", description = "Project found")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> findById(@PathVariable Long id, Authentication authentication){
        return ResponseEntity.ok(projectService.findById(id, authentication.getName()));
    }

    @Operation(summary = "Create project")
    @ApiResponse(responseCode = "201", description = "Project created sucessfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request, Authentication authentication){
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request, authentication.getName()));
    }

    @Operation(summary = "Update project")
    @ApiResponse(responseCode = "200", description = "Project updated sucessfully")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request, Authentication authentication){
        return ResponseEntity.ok(projectService.update(id,request, authentication.getName()));
    }

    @Operation(summary = "Delete project")
    @ApiResponse(responseCode = "204", description = "Project deleted sucessfully")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication){
        projectService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
