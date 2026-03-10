package com.taskapi.taskapi.dto.project;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private int taskCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
