package com.carbon.platform.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String phone;
    private String password;
    private String confirmPassword;
    private String code; // 短信验证码
}
