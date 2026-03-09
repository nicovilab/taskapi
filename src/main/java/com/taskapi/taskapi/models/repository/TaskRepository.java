package com.taskapi.taskapi.models.repository;

import com.taskapi.taskapi.models.entity.Task;
import com.taskapi.taskapi.models.entity.enumeration.TaskPriority;
import com.taskapi.taskapi.models.entity.enumeration.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId" +
            " AND (:status IS NULL OR t.status = :status)" +
            " AND (:priority IS NULL OR t.priority = :priority)")
    Page<Task> findByProjectIdWithFilters(@Param("projectId") Long projectId, @Param("status") TaskStatus status, @Param("priority") TaskPriority priority
                                          ,Pageable pageable);

    Optional<Task> findByIdAndProjectId(Long id, Long projectId);
}
