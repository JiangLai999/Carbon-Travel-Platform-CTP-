package com.carbon.platform.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String phone;
    private String newPassword;
    private String code; // 短信验证码
}
