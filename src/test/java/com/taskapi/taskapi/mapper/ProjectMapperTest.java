package com.taskapi.taskapi.mapper;

import com.taskapi.taskapi.dto.project.ProjectRequest;
import com.taskapi.taskapi.dto.project.ProjectResponse;
import com.taskapi.taskapi.entity.Project;
import com.taskapi.taskapi.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectMapperTest {

    private final ProjectMapper mapper = new ProjectMapperImpl();

    private Project project;

    @BeforeEach
    void setUp(){
        project = Project.builder()
                .id(1L)
                .name("My project")
                .description("Description")
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 6, 1, 0, 0))
                .build();
    }

    @Test
    void toResponse_mapsAllScalarFields(){
        ProjectResponse response = mapper.toResponse(project);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("My project");
        assertThat(response.getDescription()).isEqualTo("Description");
        assertThat(response.getCreatedAt()).isEqualTo(project.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(project.getUpdatedAt());
    }

    @Test
    void toResponse_withNullTaskList_returnsZeroTaskCount(){

        project.setTasks(null);

        assertThat(mapper.toResponse(project).getTaskCount()).isZero();
    }

    @Test
    void toResponse_withTwoTasks_returnsTaskCountTwo(){
        project.setTasks(List.of(new Task(), new Task()));

        assertThat(mapper.toResponse(project).getTaskCount()).isEqualTo(2);
    }

    @Test
    void toResponse_whenNullInput_returnsNull(){
        assertThat(mapper.toResponse(null)).isNull(); //mapstruct returns null if input is null
    }

    @Test
    void toEntity_mapsNameAndDescription(){
        ProjectRequest request = new ProjectRequest("New name", "New desc");

        Project entity = mapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo("New name");
        assertThat(entity.getDescription()).isEqualTo("New desc");
    }

    @Test
    void toEntity_doesNotSetIdOrUser(){
        Project entity = mapper.toEntity(new ProjectRequest("X", "Y"));

        assertThat(entity.getId()).isNull();
        assertThat(entity.getUser()).isNull();
    }

    @Test
    void toEntity_whenNullInput_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void updateEntity_overwritesNameAndDescription() {
        ProjectRequest request = new ProjectRequest("Updated name", "Updated desc");

        mapper.updateEntity(request, project);

        assertThat(project.getName()).isEqualTo("Updated name");
        assertThat(project.getDescription()).isEqualTo("Updated desc");
    }

    @Test
    void updateEntity_doesNotChangeId() {
        mapper.updateEntity(new ProjectRequest("X", "Y"), project);

        assertThat(project.getId()).isEqualTo(1L);
    }

    @Test
    void updateEntity_whenNullRequest_doesNothing() {
        String originalName = project.getName();

        mapper.updateEntity(null, project);

        assertThat(project.getName()).isEqualTo(originalName);
    }

}
