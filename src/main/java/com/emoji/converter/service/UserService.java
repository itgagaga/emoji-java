package com.emoji.converter.service;

import com.emoji.converter.model.Role;
import com.emoji.converter.model.User;
import com.emoji.converter.repository.RoleRepository;
import com.emoji.converter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public User register(String username, String password, String nickname, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }

        if (email != null && !email.isEmpty() && userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被注册");
        }

        Role defaultRole = roleRepository.findById(2)
                .orElseThrow(() -> new RuntimeException("默认角色不存在"));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setEmail(email != null ? email : "");
        user.setRoleId(2);
        user.setStatus(1);

        User savedUser = userRepository.save(user);
        log.info("用户注册成功: {} (ID: {})", username, savedUser.getId());
        return savedUser;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void updateLoginInfo(Long userId, String ip) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setLastLogin(java.time.LocalDateTime.now());
            user.setLastIp(ip);
            userRepository.save(user);
        }
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}