package com.taskapi.taskapi.mapper;

import com.taskapi.taskapi.dto.task.TaskRequest;
import com.taskapi.taskapi.dto.task.TaskResponse;
import com.taskapi.taskapi.entity.Project;
import com.taskapi.taskapi.entity.Task;
import com.taskapi.taskapi.entity.enumeration.TaskPriority;
import com.taskapi.taskapi.entity.enumeration.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


public class TaskMapperTest {

    private final TaskMapper mapper = new TaskMapperImpl();

    private Task task;
    private Project project;

    @BeforeEach
    void setUp() {
        project = Project.builder().id(10L).name("Test").build();

        task = Task.builder()
                .id(1L)
                .title("Write tests")
                .description("Desc")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.of(2025, 12, 31))
                .project(project)
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 6, 1, 0, 0))
                .build();
    }

    @Test
    void toResponse_mapsAllFields() {
        TaskResponse response = mapper.toResponse(task);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Write tests");
        assertThat(response.getDescription()).isEqualTo("Desc");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.getDueDate()).isEqualTo(LocalDate.of(2025, 12, 31));
        assertThat(response.getCreatedAt()).isEqualTo(task.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(task.getUpdatedAt());
    }

    @Test
    void toResponse_mapsProjectId() {
        assertThat(mapper.toResponse(task).getProjectId()).isEqualTo(10L);
    }

    @Test
    void toResponse_whenProjectIsNull_projectIdIsNull() {
        task.setProject(null);

        assertThat(mapper.toResponse(task).getProjectId()).isNull();
    }

    @Test
    void toResponse_whenNullInput_returnsNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void toEntity_mapsAllRequestFields() {
        TaskRequest request = new TaskRequest(
                "New task", "New desc", TaskStatus.PENDING, TaskPriority.LOW,
                LocalDate.of(2025, 11, 30));

        Task entity = mapper.toEntity(request);

        assertThat(entity.getTitle()).isEqualTo("New task");
        assertThat(entity.getDescription()).isEqualTo("New desc");
        assertThat(entity.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(entity.getPriority()).isEqualTo(TaskPriority.LOW);
        assertThat(entity.getDueDate()).isEqualTo(LocalDate.of(2025, 11, 30));
    }

    @Test
    void toEntity_doesNotSetIdOrProject() {
        Task entity = mapper.toEntity(new TaskRequest("T", null, null, null, null));

        assertThat(entity.getId()).isNull();
        assertThat(entity.getProject()).isNull();
    }

    @Test
    void updateEntity_overwritesAllMutableFields() {
        TaskRequest request = new TaskRequest(
                "Updated", "New desc", TaskStatus.COMPLETED, TaskPriority.LOW,
                LocalDate.of(2026, 1, 1));

        mapper.updateEntity(request, task);

        assertThat(task.getTitle()).isEqualTo("Updated");
        assertThat(task.getDescription()).isEqualTo("New desc");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getPriority()).isEqualTo(TaskPriority.LOW);
        assertThat(task.getDueDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    }


    @Test
    void updateEntity_doesNotChangeIdOrProject() {
        mapper.updateEntity(new TaskRequest("X", null, null, null, null), task);

        assertThat(task.getId()).isEqualTo(1L);
        assertThat(task.getProject()).isEqualTo(project);
    }

    @Test
    void updateEntity_whenNullRequest_doesNothing() {
        String originalTitle = task.getTitle();

        mapper.updateEntity(null, task);

        assertThat(task.getTitle()).isEqualTo(originalTitle);
    }
}
