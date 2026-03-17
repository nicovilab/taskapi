package com.taskapi.taskapi.service;

import com.taskapi.taskapi.dto.project.ProjectRequest;
import com.taskapi.taskapi.dto.project.ProjectResponse;
import com.taskapi.taskapi.entity.Project;
import com.taskapi.taskapi.entity.User;
import com.taskapi.taskapi.exception.ResourceNotFoundException;
import com.taskapi.taskapi.mapper.ProjectMapper;
import com.taskapi.taskapi.repository.ProjectRepository;
import com.taskapi.taskapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    public List<ProjectResponse> findAllByUser(String username) {
        User user = findUser(username);
        return projectRepository.findByUserId(user.getId()).stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    public ProjectResponse findById(Long id, String username) {
        User user = findUser(username);
        Project project = findProjectByIdAndUser(id, username);
        return projectMapper.toResponse(project);
    }

    public ProjectResponse create(ProjectRequest request, String username) {
        User user = findUser(username);
        Project project = projectMapper.toEntity(request);
        project.setUser(user);
        projectRepository.save(project);
        return projectMapper.toResponse(project);
    }

    public ProjectResponse update(Long id, ProjectRequest request, String username) {
        User user = findUser(username);
        Project project = findProjectByIdAndUser(id, username);
        projectMapper.updateEntity(request, project);
        projectRepository.save(project);
        return projectMapper.toResponse(project);
    }

    public void delete(Long id, String username) {
        User user = findUser(username);
        Project project = findProjectByIdAndUser(id, username);
        projectRepository.delete(project);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found " + username));
    }

    private Project findProjectByIdAndUser(Long id, String username) {
        User user = findUser(username);
        return projectRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project with id " + id + " not found"));
    }
}
