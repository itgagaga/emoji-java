package com.emoji.converter.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${security.auth.username:emoji}")
    private String adminUsername;

    @Value("${security.auth.password:emoji98540}")
    private String adminPassword;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (isPublicPath(path, method)) {
            return true;
        }

        if (isAuthRequired(path)) {
            String token = extractToken(request);

            if (token == null || !jwtUtil.validateToken(token)) {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "未授权访问，请先登录");
                return false;
            }
        }

        return true;
    }

    private boolean isPublicPath(String path, String method) {
        if ("GET".equals(method)) {
            if ("/".equals(path) || "/api/config".equals(path) || "/api/stats".equals(path)) {
                return true;
            }
            if (path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")) {
                return true;
            }
            if ("/aaaa/dskqrb".equals(path) || "/aaaa/dskqrb/admin".equals(path)) {
                return true;
            }
        }

        if ("POST".equals(method) && "/api/convert".equals(path)) {
            return true;
        }

        if ("POST".equals(method) && "/api/auth/login".equals(path)) {
            return true;
        }

        return false;
    }

    private boolean isAuthRequired(String path) {
        return path.startsWith("/api/admin/") || path.startsWith("/aaaa/dskqrb");
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return request.getParameter("token");
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws Exception {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    public boolean authenticate(String username, String password) {
        return adminUsername.equals(username) && adminPassword.equals(password);
    }
}
