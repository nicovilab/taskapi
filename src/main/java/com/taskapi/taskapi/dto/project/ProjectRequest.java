package com.taskapi.taskapi.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 100, message = "Name cant exceed 100 character")
    private String name;

    @Size(max = 500, message = "Description cant exceed 500 character")
    private String description;
}
