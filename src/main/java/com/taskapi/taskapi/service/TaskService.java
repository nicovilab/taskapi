package com.taskapi.taskapi.service;

import com.taskapi.taskapi.dto.task.StatusUpdateRequest;
import com.taskapi.taskapi.dto.task.TaskRequest;
import com.taskapi.taskapi.dto.task.TaskResponse;
import com.taskapi.taskapi.entity.Project;
import com.taskapi.taskapi.entity.Task;
import com.taskapi.taskapi.entity.User;
import com.taskapi.taskapi.entity.enumeration.TaskPriority;
import com.taskapi.taskapi.entity.enumeration.TaskStatus;
import com.taskapi.taskapi.exception.ResourceNotFoundException;
import com.taskapi.taskapi.mapper.TaskMapper;
import com.taskapi.taskapi.repository.ProjectRepository;
import com.taskapi.taskapi.repository.TaskRepository;
import com.taskapi.taskapi.repository.UserRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public Page<TaskResponse> findAllByProject(Long projectId, TaskStatus status, TaskPriority priority, Pageable pageable, String username){
        Project project = findProjectByIdAndUser(projectId, username);
        return taskRepository.findByProjectIdWithFilters(project.getId(), status, priority, pageable).map(taskMapper::toResponse);
    }

    public TaskResponse findById(Long projectId, Long taskId, String username){
        findProjectByIdAndUser(projectId, username);
        Task task = findTask(taskId, projectId);
        return taskMapper.toResponse(task);
    }

    public TaskResponse create(Long projectId, TaskRequest request, String username){
        Project project = findProjectByIdAndUser(projectId, username);
        Task task = taskMapper.toEntity(request);
        task.setProject(project);
        taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    public TaskResponse update(Long projectId, Long taskId, TaskRequest request, String username){
        findProjectByIdAndUser(projectId, username);
        Task task = findTask(taskId, projectId);
        taskMapper.updateEntity(request, task);
        taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    public TaskResponse updateStatus(Long projectId, Long taskId, StatusUpdateRequest request, String username){
        findProjectByIdAndUser(projectId, username);
        Task task = findTask(taskId, projectId);
        task.setStatus(request.getStatus());
        taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    public void delete(Long projectId, Long taskId, String username){
        findProjectByIdAndUser(projectId, username);
        taskRepository.delete(findTask(taskId, projectId));
    }

    private Project findProjectByIdAndUser(Long id, String username){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado " + username));
        return projectRepository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new ResourceNotFoundException("Proyecto con id " + id + " no encontrado"));
    }

    private Task findTask(Long id, Long projectId){
        return taskRepository.findByIdAndProjectId(id, projectId).orElseThrow(() -> new ResourceNotFoundException("Tarea con id " + id + " no encontrada"));
    }
}
