package com.taskapi.taskapi.repository;

import com.taskapi.taskapi.entity.Project;
import com.taskapi.taskapi.entity.Task;
import com.taskapi.taskapi.entity.User;
import com.taskapi.taskapi.entity.enumeration.Role;
import com.taskapi.taskapi.entity.enumeration.TaskPriority;
import com.taskapi.taskapi.entity.enumeration.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class TaskRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private TaskRepository    taskRepository;

    private Project project;
    private Project otherProject;


    @BeforeEach
    void setUp() {
        User user = em.persistAndFlush(User.builder()
                .username("alice").email("alice@test.com").password("password123")
                .roles(Set.of(Role.ROLE_USER)).build());

        project = em.persistAndFlush(
                Project.builder().name("Proj A").description("").user(user).build());

        otherProject = em.persistAndFlush(
                Project.builder().name("Proj B").description("").user(user).build());
    }

    @Test
    void findByProjectIdWithFilters_noFilters_returnsAllTasksOfProject() {
        em.persist(task("T1", TaskStatus.PENDING,     TaskPriority.HIGH,   project));
        em.persist(task("T2", TaskStatus.IN_PROGRESS, TaskPriority.LOW,    project));
        em.persist(task("T3", TaskStatus.COMPLETED,        TaskPriority.MEDIUM, otherProject));
        em.flush();

        Page<Task> result = taskRepository.findByProjectIdWithFilters(
                project.getId(), null, null, Pageable.unpaged());

        // just project task
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findByProjectIdWithFilters_withStatusFilter_returnsOnlyMatchingStatus() {
        em.persist(task("T1", TaskStatus.PENDING,     TaskPriority.HIGH, project));
        em.persist(task("T2", TaskStatus.IN_PROGRESS, TaskPriority.LOW,  project));
        em.flush();

        Page<Task> result = taskRepository.findByProjectIdWithFilters(
                project.getId(), TaskStatus.PENDING, null, Pageable.unpaged());

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("T1");
    }

    @Test
    void findByProjectIdWithFilters_withPriorityFilter_returnsOnlyMatchingPriority() {
        em.persist(task("T1", TaskStatus.PENDING, TaskPriority.HIGH,   project));
        em.persist(task("T2", TaskStatus.PENDING, TaskPriority.LOW,    project));
        em.persist(task("T3", TaskStatus.PENDING, TaskPriority.MEDIUM, project));
        em.flush();

        Page<Task> result = taskRepository.findByProjectIdWithFilters(
                project.getId(), null, TaskPriority.HIGH, Pageable.unpaged());

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("T1");
    }

    @Test
    void findByProjectIdWithFilters_withBothFilters_returnsCombinedResult() {
        em.persist(task("T1", TaskStatus.PENDING,     TaskPriority.HIGH, project));
        em.persist(task("T2", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, project));
        em.persist(task("T3", TaskStatus.PENDING,     TaskPriority.LOW,  project));
        em.flush();

        Page<Task> result = taskRepository.findByProjectIdWithFilters(
                project.getId(), TaskStatus.PENDING, TaskPriority.HIGH, Pageable.unpaged());

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("T1");
    }

    @Test
    void findByProjectIdWithFilters_paginationIsApplied() {
        for (int i = 0; i < 5; i++) {
            em.persist(task("T" + i, TaskStatus.PENDING, TaskPriority.MEDIUM, project));
        }
        em.flush();

        Page<Task> result = taskRepository.findByProjectIdWithFilters(
                project.getId(), null, null, PageRequest.of(0, 2));

        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    void findByProjectIdWithFilters_withNoMatch_returnsEmptyPage() {
        em.persist(task("T1", TaskStatus.PENDING, TaskPriority.MEDIUM, project));
        em.flush();

        Page<Task> result = taskRepository.findByProjectIdWithFilters(
                project.getId(), TaskStatus.COMPLETED, null, Pageable.unpaged());

        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void findByIdAndProjectId_withCorrectProject_returnsTask() {
        Task saved = em.persistAndFlush(
                task("T1", TaskStatus.PENDING, TaskPriority.MEDIUM, project));

        Optional<Task> result =
                taskRepository.findByIdAndProjectId(saved.getId(), project.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("T1");
    }

    @Test
    void findByIdAndProjectId_withWrongProject_returnsEmpty() {
        Task saved = em.persistAndFlush(
                task("T1", TaskStatus.PENDING, TaskPriority.MEDIUM, project));

        Optional<Task> result =
                taskRepository.findByIdAndProjectId(saved.getId(), otherProject.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndProjectId_withNonExistentId_returnsEmpty() {
        Optional<Task> result =
                taskRepository.findByIdAndProjectId(999L, project.getId());

        assertThat(result).isEmpty();
    }

    private Task task(String title, TaskStatus status, TaskPriority priority, Project proj) {
        return Task.builder()
                .title(title).status(status).priority(priority).project(proj).build();
    }
}
