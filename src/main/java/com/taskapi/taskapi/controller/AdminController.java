package com.taskapi.taskapi.controller;

import com.taskapi.taskapi.dto.auth.UserResponse;
import com.taskapi.taskapi.entity.User;
import com.taskapi.taskapi.mapper.ProjectMapper;
import com.taskapi.taskapi.repository.ProjectRepository;
import com.taskapi.taskapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.stream.events.EntityReference;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;


    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>>


}
