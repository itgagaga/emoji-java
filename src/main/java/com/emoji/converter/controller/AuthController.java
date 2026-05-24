package com.emoji.converter.controller;

import com.emoji.converter.interceptor.JwtUtil;
import com.emoji.converter.model.User;
import com.emoji.converter.model.dto.LoginRequest;
import com.emoji.converter.model.dto.RegisterRequest;
import com.emoji.converter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Value("${security.auth.username:emoji}")
    private String adminUsername;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            User user = userService.findByUsername(request.getUsername());

            if (user == null || !userService.validatePassword(request.getPassword(), user.getPassword())) {
                response.put("success", false);
                response.put("error", "用户名或密码错误");
                return ResponseEntity.status(401).body(response);
            }

            if (user.getStatus() == 0) {
                response.put("success", false);
                response.put("error", "账户已被禁用");
                return ResponseEntity.status(403).body(response);
            }

            String clientIp = getClientIp();
            userService.updateLoginInfo(user.getId(), clientIp);

            String token = jwtUtil.generateToken(request.getUsername());

            response.put("success", true);
            response.put("token", token);
            response.put("message", "登录成功");
            response.put("user", createUserResponse(user));
            response.put("expires_in", 3600);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "登录失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (request.getUsername() == null || request.getUsername().length() < 3) {
                response.put("success", false);
                response.put("error", "用户名至少需要3个字符");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getPassword() == null || request.getPassword().length() < 6) {
                response.put("success", false);
                response.put("error", "密码至少需要6个字符");
                return ResponseEntity.badRequest().body(response);
            }

            if (!request.getPassword().equals(request.getConfirmPassword())) {
                response.put("success", false);
                response.put("error", "两次输入的密码不一致");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userService.register(
                    request.getUsername(),
                    request.getPassword(),
                    request.getNickname(),
                    request.getEmail()
            );

            String token = jwtUtil.generateToken(user.getUsername());

            response.put("success", true);
            response.put("token", token);
            response.put("message", "注册成功");
            response.put("user", createUserResponse(user));
            response.put("expires_in", 3600);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "注册失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
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
                User user = userService.findByUsername(username);

                response.put("success", true);
                response.put("authenticated", true);
                response.put("user", user != null ? createUserResponse(user) : null);
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
                User user = userService.findByUsername(username);

                response.put("authenticated", true);
                response.put("user", user != null ? createUserResponse(user) : null);
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
        config.put("register_url", "/api/auth/register");
        config.put("session_timeout", 3600);
        config.put("allow_register", true);
        config.put("username_hint", "用户名: 3-50个字符");
        config.put("password_hint", "密码: 至少6个字符");

        return ResponseEntity.ok(config);
    }

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("email", user.getEmail());
        userInfo.put("avatarUrl", user.getAvatarUrl());
        userInfo.put("roleId", user.getRoleId());
        userInfo.put("status", user.getStatus());
        userInfo.put("mappingsCount", user.getMappingsCount());
        userInfo.put("overridesCount", user.getOverridesCount());
        userInfo.put("totalConversions", user.getTotalConversions());
        userInfo.put("lastLogin", user.getLastLogin());
        userInfo.put("createdAt", user.getCreatedAt());

        return userInfo;
    }

    private String getClientIp() {
        return "127.0.0.1";
    }
}