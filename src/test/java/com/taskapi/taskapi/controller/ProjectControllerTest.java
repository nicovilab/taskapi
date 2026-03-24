package com.taskapi.taskapi.controller;

import com.taskapi.taskapi.dto.project.ProjectRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProjectControllerTest extends BaseControllerTest {

    @Test
    void getAll_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_withValidToken_returnsEmptyList() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void create_withValidData_returns201() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProjectRequest("My project", "Description"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("My project"));
    }

    @Test
    void create_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProjectRequest("My project", ""))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_withBlankName_returns400() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProjectRequest("", "desc"))))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getById_withOwnProject_returns200() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long id = createProject(token, "My project");

        mockMvc.perform(get("/api/projects/" + id)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void getById_withAnotherUsersProject_returns404() throws Exception {
        String aliceToken = registerAndGetToken("alice", "alice@test.com", "password123");
        String bobToken   = registerAndGetToken("bob",   "bob@test.com",   "password123");

        long aliceProjectId = createProject(aliceToken, "Alice project");


        mockMvc.perform(get("/api/projects/" + aliceProjectId)
                        .header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound()); // 404
    }

    @Test
    void getById_withNonExistentId_returns404() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");

        mockMvc.perform(get("/api/projects/999999")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withOwnProject_returns200WithUpdatedData() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long id = createProject(token, "Name");

        mockMvc.perform(put("/api/projects/" + id)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProjectRequest("New name", "new desc"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"));
    }

    @Test
    void update_withAnotherUsersProject_returns404() throws Exception {
        String aliceToken = registerAndGetToken("alice", "alice@test.com", "password123");
        String bobToken   = registerAndGetToken("bob",   "bob@test.com",   "password123");

        long aliceProjectId = createProject(aliceToken, "Alice project");

        mockMvc.perform(put("/api/projects/" + aliceProjectId)
                        .header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProjectRequest("Hacked", ""))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_withOwnProject_returns204() throws Exception {
        String token = registerAndGetToken("alice", "alice@test.com", "password123");
        long id = createProject(token, "delete");

        mockMvc.perform(delete("/api/projects/" + id)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent()); // 204
    }

    @Test
    void delete_withAnotherUsersProject_returns404() throws Exception {
        String aliceToken = registerAndGetToken("alice", "alice@test.com", "password123");
        String bobToken   = registerAndGetToken("bob",   "bob@test.com",   "password123");

        long aliceProjectId = createProject(aliceToken, "Alice project");

        mockMvc.perform(delete("/api/projects/" + aliceProjectId)
                        .header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound());
    }

    //creates a project returns its id
    private long createProject(String token, String name) throws Exception {
        String body = mockMvc.perform(post("/api/projects")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProjectRequest(name, ""))))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }
}
