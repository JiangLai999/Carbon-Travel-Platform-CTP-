package com.carbon.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbon.platform.dto.ApiResponse;
import com.carbon.platform.entity.*;
import com.carbon.platform.mapper.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final CarbonPointsMapper carbonPointsMapper;
    private final PointsDetailMapper pointsDetailMapper;
    private final TravelRecordMapper travelRecordMapper;
    private final TravelModeMapper travelModeMapper;
    private final ActivityParticipationMapper activityParticipationMapper;
    private final OrderMapper orderMapper;
    private final ForumPostMapper forumPostMapper;
    private final AnnouncementMapper announcementMapper;
    private final ProductMapper productMapper;

    @GetMapping("/profile")
    public ApiResponse<?> profile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @PutMapping("/profile")
    public ApiResponse<?> updateProfile(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        if (body.containsKey("nickname")) user.setNickname(body.get("nickname"));
        if (body.containsKey("avatar")) user.setAvatar(body.get("avatar"));
        if (body.containsKey("realName")) user.setRealName(body.get("realName"));
        if (body.containsKey("idCard")) user.setIdCard(body.get("idCard"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("deliveryAddress")) user.setDeliveryAddress(body.get("deliveryAddress"));
        if (body.containsKey("deliveryName")) user.setDeliveryName(body.get("deliveryName"));
        if (body.containsKey("deliveryPhone")) user.setDeliveryPhone(body.get("deliveryPhone"));
        if (body.containsKey("province")) user.setProvince(body.get("province"));
        if (body.containsKey("city")) user.setCity(body.get("city"));
        if (body.containsKey("district")) user.setDistrict(body.get("district"));
        userMapper.updateById(user);
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @GetMapping("/points")
    public ApiResponse<?> points(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }
        CarbonPoints cp = carbonPointsMapper.selectOne(
            new LambdaQueryWrapper<CarbonPoints>().eq(CarbonPoints::getUserId, userId)
        );
        if (cp == null) {
            cp = new CarbonPoints();
            cp.setUserId(userId);
            cp.setTotalPoints(BigDecimal.ZERO);
            cp.setAvailablePoints(BigDecimal.ZERO);
            cp.setUsedPoints(BigDecimal.ZERO);
            cp.setTotalCarbon(BigDecimal.ZERO);
            carbonPointsMapper.insert(cp);
        }
        return ApiResponse.success(cp);
    }

    @GetMapping("/points/details")
    public ApiResponse<?> pointsDetails(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String type,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var wrapper = new LambdaQueryWrapper<PointsDetail>()
            .eq(PointsDetail::getUserId, userId)
            .orderByDesc(PointsDetail::getCreatedAt);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(PointsDetail::getType, type);
        }
        var result = pointsDetailMapper.selectPage(
            new Page<>(page, size),
            wrapper
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/stats")
    public ApiResponse<?> stats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CarbonPoints cp = carbonPointsMapper.selectOne(
            new LambdaQueryWrapper<CarbonPoints>().eq(CarbonPoints::getUserId, userId)
        );
        
        // 出行统计
        Long travelCount = travelRecordMapper.selectCount(
            new LambdaQueryWrapper<TravelRecord>().eq(TravelRecord::getUserId, userId)
        );
        
        // 本月出行次数
        java.time.LocalDateTime monthStart = java.time.LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        Long monthTravelCount = travelRecordMapper.selectCount(
            new LambdaQueryWrapper<TravelRecord>()
                .eq(TravelRecord::getUserId, userId)
                .ge(TravelRecord::getCreatedAt, monthStart)
        );
        
        // 总减碳量
        BigDecimal totalCarbon = cp != null ? cp.getTotalCarbon() : BigDecimal.ZERO;
        
        // 参与活动数
        Long activityCount = activityParticipationMapper.selectCount(
            new LambdaQueryWrapper<ActivityParticipation>().eq(ActivityParticipation::getUserId, userId)
        );
        
        // 兑换商品数
        Long orderCount = orderMapper.selectCount(
            new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId)
        );
        
        // 发帖数
        Long postCount = forumPostMapper.selectCount(
            new LambdaQueryWrapper<ForumPost>().eq(ForumPost::getUserId, userId)
        );
        
        // 用户排名（按积分）
        Long rank = carbonPointsMapper.selectRank(userId);
        if (rank == null) rank = 0L;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPoints", cp != null ? cp.getTotalPoints() : BigDecimal.ZERO);
        stats.put("availablePoints", cp != null ? cp.getAvailablePoints() : BigDecimal.ZERO);
        stats.put("usedPoints", cp != null ? cp.getUsedPoints() : BigDecimal.ZERO);
        stats.put("totalCarbon", totalCarbon);
        stats.put("travelCount", travelCount);
        stats.put("monthTravelCount", monthTravelCount);
        stats.put("activityCount", activityCount);
        stats.put("orderCount", orderCount);
        stats.put("postCount", postCount);
        stats.put("rank", rank);
        
        return ApiResponse.success(stats);
    }

    @GetMapping("/travel-modes")
    public ApiResponse<?> travelModes() {
        List<TravelMode> modes = travelModeMapper.selectList(
            new LambdaQueryWrapper<TravelMode>()
                .eq(TravelMode::getStatus, 1)
                .orderByAsc(TravelMode::getSortOrder)
        );
        return ApiResponse.success(modes);
    }

    @GetMapping("/announcements")
    public ApiResponse<?> announcements(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "5") int size) {
        var result = announcementMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getCreatedAt)
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/announcements/{id}")
    public ApiResponse<?> announcementDetail(@PathVariable Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getStatus() != 1) {
            return ApiResponse.error("公告不存在");
        }
        return ApiResponse.success(announcement);
    }

    @GetMapping("/activities/joined")
    public ApiResponse<?> joinedActivities(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var result = activityParticipationMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<ActivityParticipation>()
                .eq(ActivityParticipation::getUserId, userId)
                .orderByDesc(ActivityParticipation::getCreatedAt)
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/orders")
    public ApiResponse<?> orders(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) Long id,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (id != null) {
            Order order = orderMapper.selectById(id);
            if (order != null && order.getUserId().equals(userId)) {
                Product product = productMapper.selectById(order.getProductId());
                if (product != null) {
                    order.setProductName(product.getName());
                }
                return ApiResponse.success(order);
            }
            return ApiResponse.error("订单不存在");
        }
        var result = orderMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt)
        );
        result.getRecords().forEach(order -> {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                order.setProductName(product.getName());
            }
        });
        return ApiResponse.success(result);
    }

    @GetMapping("/rank")
    public ApiResponse<?> rank(@RequestParam(defaultValue = "10") int limit) {
        List<CarbonPoints> topUsers = carbonPointsMapper.selectList(
            new LambdaQueryWrapper<CarbonPoints>()
                .orderByDesc(CarbonPoints::getTotalPoints)
                .last("LIMIT " + limit)
        );
        
        List<Map<String, Object>> rankList = new java.util.ArrayList<>();
        for (int i = 0; i < topUsers.size(); i++) {
            CarbonPoints cp = topUsers.get(i);
            User user = userMapper.selectById(cp.getUserId());
            if (user != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("rank", i + 1);
                item.put("userId", user.getId());
                item.put("nickname", user.getNickname());
                item.put("avatar", user.getAvatar());
                item.put("totalPoints", cp.getTotalPoints());
                item.put("totalCarbon", cp.getTotalCarbon());
                rankList.add(item);
            }
        }
        return ApiResponse.success(rankList);
    }
}
