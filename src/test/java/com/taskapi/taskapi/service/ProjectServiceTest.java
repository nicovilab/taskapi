package com.taskapi.taskapi.service;

import com.taskapi.taskapi.dto.project.ProjectRequest;
import com.taskapi.taskapi.dto.project.ProjectResponse;
import com.taskapi.taskapi.entity.Project;
import com.taskapi.taskapi.entity.User;
import com.taskapi.taskapi.exception.ResourceNotFoundException;
import com.taskapi.taskapi.mapper.ProjectMapper;
import com.taskapi.taskapi.repository.ProjectRepository;
import com.taskapi.taskapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private User user;
    private Project project;
    private ProjectResponse projectResponse;
    private ProjectRequest projectRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@mail.com")
                .build();

        project = Project.builder()
                .id(10L)
                .name("My project")
                .description("description")
                .user(user)
                .build();

        projectResponse = ProjectResponse.builder()
                .id(10L)
                .name("My project")
                .description("description")
                .taskCount(0)
                .build();

        projectRequest = new ProjectRequest("My project", "description");
    }

    @Test
    void findAllByUser_returnsMappedList() {

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(projectRepository.findByUserId(1L)).thenReturn(List.of(project));
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);

        List<ProjectResponse> result = projectService.findAllByUser("alice");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("My project");
    }

    @Test
    void findAllByUser_WhenUserNotFound_throwException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findAllByUser("ghost")).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void findById_returnsProject() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(project));
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);

        ProjectResponse result = projectService.findById(10L, "alice");

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void findById_whenProjectBelongsToAnotherUser_throws404() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty()); //returns empty because is not an Alice project

        assertThatThrownBy(() -> projectService.findById(10L, "alice")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_savesAndReturnsMappedResponse() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(projectMapper.toEntity(projectRequest)).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);

        ProjectResponse result = projectService.create(projectRequest, "alice");

        verify(projectRepository).save(project);
        assertThat(result.getName()).isEqualTo("My project");
    }

    @Test
    void create_assignsUserToProject() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(projectMapper.toEntity(projectRequest)).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);

        projectService.create(projectRequest, "alice");

        assertThat(project.getUser()).isEqualTo(user);
    }

    @Test
    void update_modifiesAndReturnsProject() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(project));
        doNothing().when(projectMapper).updateEntity(projectRequest, project); //updateEntity is void so we use doNothing
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);

        ProjectResponse result = projectService.update(10L, projectRequest, "alice");

        verify(projectMapper).updateEntity(projectRequest, project);
        verify(projectRepository).save(project);
        assertThat(result).isNotNull();
    }

    @Test
    void update_whenProjectNotFound_throws404() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.update(99L, projectRequest, "alice")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_callsRepositoryDelete(){
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(project));

        projectService.delete(10L, "alice");

        verify(projectRepository).delete(project);
    }

    @Test
    void delete_whenProjectNotFound_throws404(){
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.delete(99L, "alice")).isInstanceOf(ResourceNotFoundException.class);

        verify(projectRepository, never()).delete(any()); // never verifies that delete was never called
    }
}
