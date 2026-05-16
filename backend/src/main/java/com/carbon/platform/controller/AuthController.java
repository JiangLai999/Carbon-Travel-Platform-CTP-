package com.carbon.platform.controller;

import com.carbon.platform.dto.*;
import com.carbon.platform.service.AuthService;
import com.carbon.platform.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SmsService smsService;

    @PostMapping("/send-code")
    public ApiResponse<Map<String, String>> sendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }
        String code = smsService.sendCode(phone);
        return ApiResponse.success(Map.of("code", code, "msg", "验证码已发送，60秒内有效"));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@RequestBody RegisterRequest req) {
        if (!smsService.verifyCode(req.getPhone(), req.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }
        return ApiResponse.success(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest req) {
        if ("code".equals(req.getLoginType())) {
            if (!smsService.verifyCode(req.getPhone(), req.getCode())) {
                throw new RuntimeException("验证码错误或已过期");
            }
            return ApiResponse.success(authService.loginByPhone(req.getPhone()));
        }
        return ApiResponse.success(authService.login(req));
    }

    @PostMapping("/admin/login")
    public ApiResponse<LoginResponse> adminLogin(@RequestBody LoginRequest req) {
        return ApiResponse.success(authService.adminLogin(req));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest req) {
        if (!smsService.verifyCode(req.getPhone(), req.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }
        authService.resetPassword(req);
        return ApiResponse.success(null);
    }
}
