package com.taskapi.taskapi.models.repository;

import com.taskapi.taskapi.models.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByUserId(Long id);

    Optional<Project> findByIdAndUserId(Long id, Long userId);
}
