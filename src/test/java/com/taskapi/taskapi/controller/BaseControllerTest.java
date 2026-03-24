package com.taskapi.taskapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskapi.taskapi.dto.auth.RegisterRequest;
import com.taskapi.taskapi.repository.ProjectRepository;
import com.taskapi.taskapi.repository.TaskRepository;
import com.taskapi.taskapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected TaskRepository taskRepository;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected String registerAndGetToken(String username, String email, String password)
            throws Exception {
        RegisterRequest request = new RegisterRequest(username, email, password);

        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()          // get MvcResult
                .getResponse()        // get HttpServletResponse
                .getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

}
