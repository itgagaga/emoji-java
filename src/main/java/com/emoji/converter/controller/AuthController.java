package com.emoji.converter.controller;

import com.emoji.converter.interceptor.AuthInterceptor;
import com.emoji.converter.interceptor.JwtUtil;
import com.emoji.converter.model.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthInterceptor authInterceptor;

    @Value("${security.auth.username:emoji}")
    private String adminUsername;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (authInterceptor.authenticate(request.getUsername(), request.getPassword())) {
            String token = jwtUtil.generateToken(request.getUsername());

            response.put("success", true);
            response.put("token", token);
            response.put("message", "登录成功");
            response.put("username", request.getUsername());
            response.put("expires_in", 3600);

            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("error", "用户名或密码错误");

            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "已登出");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                response.put("success", true);
                response.put("authenticated", true);
                response.put("username", username);
                return ResponseEntity.ok(response);
            }
        }

        response.put("success", false);
        response.put("authenticated", false);
        response.put("error", "无效的 Token");

        return ResponseEntity.status(401).body(response);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                response.put("authenticated", true);
                response.put("username", username);
                Map<String, Object> sessionInfo = new HashMap<>();
                sessionInfo.put("username", username);
                response.put("session_info", sessionInfo);
                return ResponseEntity.ok(response);
            }
        }

        response.put("authenticated", false);
        response.put("message", "未登录或会话已过期");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getAuthConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("has_auth_system", true);
        config.put("login_url", "/aaaa/dskqrb");
        config.put("session_timeout", 3600);
        config.put("max_login_attempts", 5);
        config.put("lockout_duration", 900);
        config.put("username_hint", "用户名: " + adminUsername);

        return ResponseEntity.ok(config);
    }
}
