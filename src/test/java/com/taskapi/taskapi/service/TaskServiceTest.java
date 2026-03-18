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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Project project;
    private Task task;
    private TaskResponse taskResponse;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp(){
        user = User.builder()
                .id(1L)
                .username("alice")
                .build();

        project = Project.builder()
                .id(10L)
                .name("Project")
                .user(user)
                .build();

        task = Task.builder()
                .id(100L)
                .title("Doing something")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .project(project)
                .build();

        taskResponse = TaskResponse.builder()
                .id(100L)
                .title("Doing something")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .projectId(10L)
                .build();

        taskRequest = new TaskRequest("Doing something", "desc", TaskStatus.PENDING, TaskPriority.MEDIUM, null);
    }

    @Test
    void findAllByProject_returnsMappedPage(){
        stubFindProjectAndUser();

        Pageable pageable = PageRequest.of(0, 10);

        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findByProjectIdWithFilters(10L, null, null, pageable)).thenReturn(taskPage);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        Page<TaskResponse> result = taskService.findAllByProject(10L, null, null, pageable, "alice");
        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Doing something");
    }

    @Test
    void findAllByProject_withFilters_passesFiltersToRepository(){
        stubFindProjectAndUser();

        Pageable pageable = PageRequest.of(0,10);

        when(taskRepository.findByProjectIdWithFilters(10L, TaskStatus.PENDING, TaskPriority.HIGH, pageable)).thenReturn(Page.empty());
        taskService.findAllByProject(10L, TaskStatus.PENDING, TaskPriority.HIGH, pageable, "alice");

        verify(taskRepository).findByProjectIdWithFilters(10L, TaskStatus.PENDING, TaskPriority.HIGH, pageable);
    }

    @Test
    void findAllByProject_whenProjectNotFound_throws404(){
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findAllByProject(10L, null, null, Pageable.unpaged(), "alice"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_returnsTask(){
        stubFindProjectAndUser();

        when(taskRepository.findByIdAndProjectId(100L, 10L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.findById(10L, 100L, "alice");

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void findById_whenTaskNotFound_throws404(){
        stubFindProjectAndUser();

        when(taskRepository.findByIdAndProjectId(999L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(10L, 999L, "alice")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_savesAndReturnsTask(){
        stubFindProjectAndUser();

        when(taskMapper.toEntity(taskRequest)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.create(10L, taskRequest, "alice");
        verify(taskRepository).save(task);
        assertThat(result.getTitle()).isEqualTo("Doing something");
    }

    @Test
    void create_assignsProjectToTask(){
        stubFindProjectAndUser();

        when(taskMapper.toEntity(taskRequest)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        taskService.create(10L, taskRequest, "alice");

        assertThat(task.getProject()).isEqualTo(project);
    }

    @Test
    void update_modifiesAndSavesTask(){
        stubFindProjectAndUser();
        when(taskRepository.findByIdAndProjectId(100L, 10L)).thenReturn(Optional.of(task));
        doNothing().when(taskMapper).updateEntity(taskRequest, task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        taskService.update(10L, 100L,  taskRequest, "alice");

        verify(taskMapper).updateEntity(taskRequest, task);
        verify(taskRepository).save(task);
    }

    @Test
    void update_whenTaskNotFound_throws404(){
        stubFindProjectAndUser();
        when(taskRepository.findByIdAndProjectId(999L, 10L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> taskService.update(10L, 999L, taskRequest, "alice")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_changesStatusAndSaves(){
        stubFindProjectAndUser();
        when(taskRepository.findByIdAndProjectId(100L, 10L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        StatusUpdateRequest req = new StatusUpdateRequest(TaskStatus.IN_PROGRESS);
        taskService.updateStatus(10L, 100L, req, "alice");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository).save(task);
    }

    @Test
    void delete_callsRepositoryDelete(){
        stubFindProjectAndUser();
        when(taskRepository.findByIdAndProjectId(100L, 10L)).thenReturn(Optional.of(task));

        taskService.delete(10L, 100L, "alice");

        verify(taskRepository).delete(task);
    }

    @Test
    void delete_whenTaskNotFound_throws404(){
        stubFindProjectAndUser();
        when(taskRepository.findByIdAndProjectId(999L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(10L, 999L, "alice")).isInstanceOf(ResourceNotFoundException.class);
        verify(taskRepository, never()).delete(task);
    }

    private void stubFindProjectAndUser(){
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(project));
    }
}
