package com.carbon.platform.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String phone;
    private String password;   // 密码登录时使用
    private String code;       // 验证码登录时使用
    private String loginType;  // "password" 或 "code"
}
