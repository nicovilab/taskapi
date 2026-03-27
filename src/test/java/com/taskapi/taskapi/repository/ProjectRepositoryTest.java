package com.taskapi.taskapi.repository;

import com.taskapi.taskapi.entity.Project;
import com.taskapi.taskapi.entity.User;
import com.taskapi.taskapi.entity.enumeration.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ProjectRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProjectRepository projectRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {

        alice = em.persistAndFlush(User.builder()
                .username("alice").email("alice@test.com").password("pass")
                .roles(Set.of(Role.ROLE_USER)).build());

        bob = em.persistAndFlush(User.builder()
                .username("bob").email("bob@test.com").password("pass")
                .roles(Set.of(Role.ROLE_USER)).build());
    }

    @Test
    void findByUserId_returnsOnlyProjectsOfThatUser() {
        em.persist(project("Project Alice 1", alice));
        em.persist(project("Project Alice 2", alice));
        em.persist(project("Project Bob",     bob));
        em.flush();

        List<Project> result = projectRepository.findByUserId(alice.getId());

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.getUser().getId().equals(alice.getId()));
    }

    @Test
    void findByUserId_whenUserHasNoProjects_returnsEmptyList() {
        assertThat(projectRepository.findByUserId(alice.getId())).isEmpty();
    }

    @Test
    void findByIdAndUserId_withMatchingOwner_returnsProject() {
        Project saved = em.persistAndFlush(project("proj", alice));

        Optional<Project> result =
                projectRepository.findByIdAndUserId(saved.getId(), alice.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("proj");
    }

    @Test
    void findByIdAndUserId_withWrongOwner_returnsEmpty() {
        Project saved = em.persistAndFlush(project("alice proj", alice));

        Optional<Project> result =
                projectRepository.findByIdAndUserId(saved.getId(), bob.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndUserId_withNonExistentId_returnsEmpty() {
        Optional<Project> result =
                projectRepository.findByIdAndUserId(999L, alice.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void save_persistsProjectAndGeneratesId() {
        Project saved = projectRepository.save(project("proj", alice));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void delete_removesProject() {
        Project saved = em.persistAndFlush(project("delete", alice));

        projectRepository.delete(saved);

        assertThat(projectRepository.findById(saved.getId())).isEmpty();
    }

    private Project project(String name, User owner) {
        return Project.builder()
                .name(name).description("desc").user(owner).build();
    }
}
