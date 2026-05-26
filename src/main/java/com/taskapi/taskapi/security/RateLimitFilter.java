package com.taskapi.taskapi.security;

import com.taskapi.taskapi.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.annotation.Priority;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Priority(1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(properties.getCapacity())
                        .refillGreedy(properties.getCapacity(), Duration.ofMinutes(properties.getRefillDuration()))
                        .build())
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String key = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : request.getRemoteAddr();


        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket());

        if (bucket.tryConsume(1)){
            filterChain.doFilter(request, response);
        }else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                        {
                            "status": 429,
                            "error": "Too Many Requests",
                            "message": "Has excedido el limite de peticiones. Intentalo en 1 minuto."
                        }
                    """);
        }
    }
}
