package com.taskapi.taskapi.controller;

import com.taskapi.taskapi.dto.auth.UserResponse;
import com.taskapi.taskapi.dto.project.ProjectResponse;
import com.taskapi.taskapi.entity.User;
import com.taskapi.taskapi.mapper.ProjectMapper;
import com.taskapi.taskapi.mapper.UserMapper;
import com.taskapi.taskapi.repository.ProjectRepository;
import com.taskapi.taskapi.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin operations, requires ADMIN role")
public class AdminController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;

    @Operation(summary = "Get all users", description = "Returns a list of all registered users")
    @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied, admin only")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        List<UserResponse> users = userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get all projects", description = "Returns a list of all projects")
    @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied, admin only")
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponse>> getAllProjects(){
        List<ProjectResponse> projects = projectRepository.findAll().stream()
                .map(projectMapper::toResponse)
                .toList();
        return ResponseEntity.ok(projects);
    }


}
