package com.taskapi.taskapi.controller;

import com.taskapi.taskapi.dto.task.StatusUpdateRequest;
import com.taskapi.taskapi.dto.task.TaskRequest;
import com.taskapi.taskapi.dto.task.TaskResponse;
import com.taskapi.taskapi.entity.enumeration.TaskPriority;
import com.taskapi.taskapi.entity.enumeration.TaskStatus;
import com.taskapi.taskapi.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> findAll(@PathVariable Long projectId, @RequestParam(required = false) TaskStatus status,
                                                      @RequestParam(required = false) TaskPriority priority,
                                                      @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
                                                      Authentication authentication){
        return ResponseEntity.ok(taskService.findAllByProject(projectId, status, priority, pageable, authentication.getName()));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> findById(@PathVariable Long projectId, @PathVariable Long taskId, Authentication authentication){
        return ResponseEntity.ok(taskService.findById(projectId, taskId, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@PathVariable Long projectId, @Valid @RequestBody TaskRequest request, Authentication authentication){
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(projectId, request, authentication.getName()));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long projectId, @PathVariable Long taskId, @Valid @RequestBody TaskRequest request,
                                               Authentication authentication){
        return ResponseEntity.ok(taskService.update(projectId, taskId, request, authentication.getName()));
    }


    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateStatus(@PathVariable Long projectId, @PathVariable Long taskId, @Valid @RequestBody StatusUpdateRequest request,
                                                     Authentication authentication){
        return ResponseEntity.ok(taskService.updateStatus(projectId, taskId, request, authentication.getName()));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long taskId, Authentication authentication){
        taskService.delete(projectId, taskId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
