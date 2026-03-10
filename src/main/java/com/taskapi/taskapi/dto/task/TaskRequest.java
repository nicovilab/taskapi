package com.taskapi.taskapi.dto.task;

import com.taskapi.taskapi.entity.enumeration.TaskPriority;
import com.taskapi.taskapi.entity.enumeration.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "El titulo de la tarea es obligatorio")
    @Size(max = 150, message = "El titulo no puede superar los 150 caracteres")
    private String title;

    @Size(max = 1000, message = "La descripcion no puede superar los 1000 caraceteres")
    private String description;

    private TaskStatus status;
    private TaskPriority priority;

    @FutureOrPresent(message = "La fecha límite debe ser hoy o una fecha futura")
    private LocalDate dueDate;
}
