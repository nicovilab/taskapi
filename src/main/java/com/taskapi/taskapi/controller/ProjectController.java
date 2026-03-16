package com.taskapi.taskapi.controller;

import com.taskapi.taskapi.dto.project.ProjectRequest;
import com.taskapi.taskapi.dto.project.ProjectResponse;
import com.taskapi.taskapi.service.ProjectService;
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
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> findAll(Authentication authentication){
        return ResponseEntity.ok(projectService.findAllByUser(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> findById(@PathVariable Long id, Authentication authentication){
        return ResponseEntity.ok(projectService.findById(id, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request, Authentication authentication){
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request, Authentication authentication){
        return ResponseEntity.ok(projectService.update(id,request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication){
        projectService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
