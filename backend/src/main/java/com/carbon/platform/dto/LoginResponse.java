package com.carbon.platform.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String phone;
    private String nickname;
    private String avatar;
    private String role;
    private String token;
}
