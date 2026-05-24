package com.emoji.converter.model.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String confirmPassword;
    private String nickname;
    private String email;
}