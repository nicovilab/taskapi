package com.taskapi.taskapi.dto.auth;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private Boolean enabled;
    private LocalDateTime createdAt;
}
