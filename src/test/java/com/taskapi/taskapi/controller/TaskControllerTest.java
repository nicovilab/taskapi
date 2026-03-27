package com.taskapi.taskapi.controller;

import com.taskapi.taskapi.dto.project.ProjectRequest;
import com.taskapi.taskapi.dto.task.StatusUpdateRequest;
import com.taskapi.taskapi.dto.task.TaskRequest;
import com.taskapi.taskapi.entity.enumeration.TaskPriority;
import com.taskapi.taskapi.entity.enumeration.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TaskControllerTest extends BaseControllerTest{

    @Test
    void getAll_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/projects/1/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_withValidToken_returnsPagedResult() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long projectId = createProject(token, "Project");

        mockMvc.perform(get("/api/projects/" + projectId + "/tasks")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAll_withStatusFilter_returnsOnlyMatchingTasks() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long projectId = createProject(token, "Proj");

        createTask(token, projectId, "Task PENDING",     TaskStatus.PENDING,     TaskPriority.HIGH);
        createTask(token, projectId, "Task IN_PROGRESS", TaskStatus.IN_PROGRESS, TaskPriority.LOW);

        mockMvc.perform(get("/api/projects/" + projectId + "/tasks")
                        .param("status", "PENDING")   // query param ?status=PENDING
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Task PENDING"));
    }

    @Test
    void getAll_withPriorityFilter_returnsOnlyMatchingTasks() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long projectId = createProject(token, "Proj");

        createTask(token, projectId, "Task HIGH", TaskStatus.PENDING, TaskPriority.HIGH);
        createTask(token, projectId, "Task LOW",  TaskStatus.PENDING, TaskPriority.LOW);

        mockMvc.perform(get("/api/projects/" + projectId + "/tasks")
                        .param("priority", "HIGH")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Task HIGH"));
    }

    @Test
    void getAll_paginationIsRespected() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long projectId = createProject(token, "Proj");

        for (int i = 0; i < 5; i++) {
            createTask(token, projectId, "Task " + i, TaskStatus.PENDING, TaskPriority.MEDIUM);
        }

        mockMvc.perform(get("/api/projects/" + projectId + "/tasks")
                        .param("size", "2")
                        .param("page", "0")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.content.length()").value(2)) // 2 in the same page
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void create_withValidData_returns201() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long projectId = createProject(token, "Proj");

        TaskRequest request = new TaskRequest(
                "Task", "desc", TaskStatus.PENDING, TaskPriority.HIGH, null);

        mockMvc.perform(post("/api/projects/" + projectId + "/tasks")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Task"))
                .andExpect(jsonPath("$.projectId").value(projectId));
    }

    @Test
    void create_withBlankTitle_returns400() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long projectId = createProject(token, "Proj");

        TaskRequest bad = new TaskRequest("", null, null, null, null);

        mockMvc.perform(post("/api/projects/" + projectId + "/tasks")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withPastDueDate_returns400() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long projectId = createProject(token, "Proj");

        TaskRequest bad = new TaskRequest(
                "Title", null, null, null, LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/api/projects/" + projectId + "/tasks")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getById_withOwnTask_returns200() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long projectId = createProject(token, "Proj");
        long taskId = createTask(token, projectId, "Task", TaskStatus.PENDING, TaskPriority.MEDIUM);

        mockMvc.perform(get("/api/projects/" + projectId + "/tasks/" + taskId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId));
    }

    @Test
    void getById_withAnotherUsersTask_returns404() throws Exception {
        String aliceToken = registerAndGetToken("alice", "alice@test.com", "password123");
        String bobToken   = registerAndGetToken("bob",   "bob@test.com",   "password123");

        long aliceProjId = createProject(aliceToken, "Proj de Alice");
        long aliceTaskId = createTask(aliceToken, aliceProjId, "Tarea de Alice",
                TaskStatus.PENDING, TaskPriority.LOW);

        mockMvc.perform(get("/api/projects/" + aliceProjId + "/tasks/" + aliceTaskId)
                        .header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withOwnTask_returns200WithUpdatedData() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long projectId = createProject(token, "Proj");
        long taskId = createTask(token, projectId, "Title",
                TaskStatus.PENDING, TaskPriority.LOW);

        TaskRequest updated = new TaskRequest(
                "New title", "New desc", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, null);

        mockMvc.perform(put("/api/projects/" + projectId + "/tasks/" + taskId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateStatus_withValidStatus_returns200() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long projectId = createProject(token, "Proj");
        long taskId = createTask(token, projectId, "Task",
                TaskStatus.PENDING, TaskPriority.MEDIUM);

        StatusUpdateRequest req = new StatusUpdateRequest(TaskStatus.COMPLETED);

        mockMvc.perform(patch("/api/projects/" + projectId + "/tasks/" + taskId + "/status")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void updateStatus_withNullStatus_returns400() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long projectId = createProject(token, "Proj");
        long taskId = createTask(token, projectId, "Task",
                TaskStatus.PENDING, TaskPriority.MEDIUM);

        mockMvc.perform(patch("/api/projects/" + projectId + "/tasks/" + taskId + "/status")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_withOwnTask_returns204() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long projectId = createProject(token, "Proj");
        long taskId = createTask(token, projectId, "Delete",
                TaskStatus.PENDING, TaskPriority.MEDIUM);

        mockMvc.perform(delete("/api/projects/" + projectId + "/tasks/" + taskId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    //create project return its id
    private long createProject(String token, String name) throws Exception {
        String body = mockMvc.perform(post("/api/projects")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProjectRequest(name, ""))))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    //create task return its id
    private long createTask(String token, long projectId, String title,
                            TaskStatus status, TaskPriority priority) throws Exception {
        TaskRequest req = new TaskRequest(title, null, status, priority, null);

        String body = mockMvc.perform(post("/api/projects/" + projectId + "/tasks")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }
}
