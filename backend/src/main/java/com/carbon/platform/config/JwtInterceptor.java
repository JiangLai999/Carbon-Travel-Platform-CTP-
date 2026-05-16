package com.carbon.platform.config;

import com.carbon.platform.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    private static final String[] PUBLIC_GET_PREFIXES = {
        "/activities",
        "/forum/posts",
        "/forum/sections",
        "/shop/products",
        "/user/announcements"
    };

    private static final String ADMIN_PATH_PREFIX = "/admin/";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getServletPath();
        String method = request.getMethod();
        String token = request.getHeader("Authorization");
        
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            return handlePublicOrUnauthorized(path, method, response);
        }
        
        token = token.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return handlePublicOrUnauthorized(path, method, response);
        }
        
        Long userId = jwtUtils.getUserId(token);
        String role = jwtUtils.getRole(token);
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);
        
        if (path.startsWith(ADMIN_PATH_PREFIX) && !"admin".equals(role)) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"无权访问管理员接口\"}");
            return false;
        }
        
        return true;
    }
    
    private boolean handlePublicOrUnauthorized(String path, String method, HttpServletResponse response) throws Exception {
        if ("GET".equals(method)) {
            for (String prefix : PUBLIC_GET_PREFIXES) {
                if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                    return true;
                }
            }
        }
        
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录\"}");
        return false;
    }
}
