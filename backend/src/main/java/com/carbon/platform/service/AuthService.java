package com.carbon.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carbon.platform.dto.*;
import com.carbon.platform.entity.*;
import com.carbon.platform.mapper.*;
import com.carbon.platform.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final CarbonPointsMapper carbonPointsMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("两次密码不一致");
        }
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone())
        );
        if (count > 0) {
            throw new RuntimeException("手机号已注册");
        }
        User user = new User();
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname("用户" + req.getPhone().substring(7));
        user.setStatus(1);
        user.setRole("user");
        userMapper.insert(user);

        CarbonPoints points = new CarbonPoints();
        points.setUserId(user.getId());
        points.setTotalPoints(java.math.BigDecimal.ZERO);
        points.setAvailablePoints(java.math.BigDecimal.ZERO);
        points.setUsedPoints(java.math.BigDecimal.ZERO);
        carbonPointsMapper.insert(points);

        return buildLoginResponse(user);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone())
        );
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("手机号或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        return buildLoginResponse(user);
    }

    public LoginResponse adminLogin(LoginRequest req) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone())
        );
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("手机号或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        if (!"admin".equals(user.getRole())) {
            throw new RuntimeException("非管理员账号，无权登录后台");
        }
        return buildLoginResponse(user);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone())
        );
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userMapper.updateById(user);
    }

    // 验证码登录：手机号存在则直接登录，不存在则自动注册
    @Transactional
    public LoginResponse loginByPhone(String phone) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        );
        if (user == null) {
            // 自动注册
            user = new User();
            user.setPhone(phone);
            user.setPassword(passwordEncoder.encode(phone)); // 临时密码
            user.setNickname("用户" + phone.substring(7));
            user.setStatus(1);
            user.setRole("user");
            userMapper.insert(user);
            CarbonPoints points = new CarbonPoints();
            points.setUserId(user.getId());
            points.setTotalPoints(java.math.BigDecimal.ZERO);
            points.setAvailablePoints(java.math.BigDecimal.ZERO);
            points.setUsedPoints(java.math.BigDecimal.ZERO);
            carbonPointsMapper.insert(points);
        }
        if (user.getStatus() == 0) throw new RuntimeException("账号已被禁用");
        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getId());
        resp.setPhone(user.getPhone());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setRole(user.getRole());
        resp.setToken(jwtUtils.generateToken(user.getId(), user.getRole()));
        return resp;
    }
}
