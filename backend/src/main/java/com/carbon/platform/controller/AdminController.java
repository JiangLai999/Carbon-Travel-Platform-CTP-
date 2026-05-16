package com.carbon.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbon.platform.dto.ApiResponse;
import com.carbon.platform.entity.*;
import com.carbon.platform.mapper.*;
import com.carbon.platform.service.ForumService;
import com.carbon.platform.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final TravelRecordMapper travelRecordMapper;
    private final CarbonPointsMapper carbonPointsMapper;
    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ActivityMapper activityMapper;
    private final AnnouncementMapper announcementMapper;
    private final ForumPostMapper forumPostMapper;
    private final PointsRuleMapper pointsRuleMapper;
    private final TravelModeMapper travelModeMapper;
    private final ActivityParticipationMapper activityParticipationMapper;
    private final OrderMapper orderMapper;
    private final ForumCommentMapper forumCommentMapper;
    private final ForumService forumService;
    private final MessageMapper messageMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/stats")
    public ApiResponse<?> stats() {
        Map<String, Object> data = new HashMap<>();
        
        // 用户统计
        Long totalUsers = userMapper.selectCount(null);
        data.put("totalUsers", totalUsers);
        
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Long todayNewUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>().ge(User::getCreatedAt, todayStart));
        data.put("todayNewUsers", todayNewUsers);
        
        Long activeUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        data.put("activeUsers", activeUsers);
        
        Long bannedUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>().eq(User::getStatus, 0));
        data.put("bannedUsers", bannedUsers);
        
        // 出行统计
        data.put("totalRecords", travelRecordMapper.selectCount(null));
        data.put("pendingRecords", travelRecordMapper.selectCount(
            new LambdaQueryWrapper<TravelRecord>().eq(TravelRecord::getStatus, 0)));
        data.put("todayRecords", travelRecordMapper.selectCount(
            new LambdaQueryWrapper<TravelRecord>().ge(TravelRecord::getCreatedAt, todayStart)));
        
        // 商品和活动
        data.put("totalProducts", productMapper.selectCount(null));
        data.put("totalActivities", activityMapper.selectCount(null));
        
        // 论坛统计
        Long totalPosts = forumPostMapper.selectCount(null);
        data.put("totalPosts", totalPosts);
        
        Long todayPosts = forumPostMapper.selectCount(
            new LambdaQueryWrapper<ForumPost>().ge(ForumPost::getCreatedAt, todayStart));
        data.put("todayPosts", todayPosts);
        
        Long pendingComments = forumCommentMapper.selectCount(
            new LambdaQueryWrapper<ForumComment>().gt(ForumComment::getReportCount, 0));
        data.put("pendingReports", pendingComments);
        
        // 订单统计
        data.put("pendingOrders", orderMapper.selectCount(
            new LambdaQueryWrapper<Order>().eq(Order::getStatus, 0)));
        
        // 碳积分统计
        data.put("totalPoints", carbonPointsMapper.selectTotalPoints());
        data.put("totalCarbon", carbonPointsMapper.selectTotalCarbon());
        
        // 活动参与统计
        data.put("pendingProofs", activityParticipationMapper.selectCount(
            new LambdaQueryWrapper<ActivityParticipation>().eq(ActivityParticipation::getStatus, 0)));
        
        return ApiResponse.success(data);
    }

    @GetMapping("/top-users")
    public ApiResponse<?> topUsers() {
        // 查询碳积分排行榜前5名
        List<CarbonPoints> topPoints = carbonPointsMapper.selectList(
            new LambdaQueryWrapper<CarbonPoints>()
                .orderByDesc(CarbonPoints::getTotalCarbon)
                .last("LIMIT 5")
        );
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (CarbonPoints cp : topPoints) {
            User user = userMapper.selectById(cp.getUserId());
            if (user != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", user.getId());
                String displayName = user.getNickname() != null ? user.getNickname() 
                    : (user.getRealName() != null ? user.getRealName() : "用户" + user.getId());
                item.put("name", displayName);
                item.put("avatar", user.getAvatar());
                item.put("totalCarbon", cp.getTotalCarbon() != null ? cp.getTotalCarbon() : 0);
                item.put("travelCount", travelRecordMapper.selectCountByUserId(user.getId()));
                result.add(item);
            }
        }
        
        return ApiResponse.success(result);
    }

    @GetMapping("/mode-stats")
    public ApiResponse<?> modeStats() {
        // 获取出行方式列表
        List<TravelMode> modes = travelModeMapper.selectList(
            new LambdaQueryWrapper<TravelMode>().orderByAsc(TravelMode::getSortOrder)
        );
        
        // 获取各出行方式的记录数
        List<Map<String, Object>> modeCounts = travelRecordMapper.selectCountByMode();
        Map<Long, Long> countMap = new HashMap<>();
        Long totalCount = 0L;
        for (Map<String, Object> mc : modeCounts) {
            Long modeId = ((Number) mc.get("travel_mode_id")).longValue();
            Long count = ((Number) mc.get("count")).longValue();
            countMap.put(modeId, count);
            totalCount += count;
        }
        
        // 定义颜色
        String[] colors = {"#10b981", "#3b82f6", "#f59e0b", "#8b5cf6", "#6b7280", "#ec4899"};
        
        List<Map<String, Object>> result = new ArrayList<>();
        int colorIndex = 0;
        for (TravelMode mode : modes) {
            Long count = countMap.getOrDefault(mode.getId(), 0L);
            if (count > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", mode.getName());
                item.put("value", count);
                item.put("color", colors[colorIndex % colors.length]);
                result.add(item);
                colorIndex++;
            }
        }
        
        return ApiResponse.success(result);
    }

    @GetMapping("/weekly-stats")
    public ApiResponse<?> weeklyStats(@RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        if (days <= 7) {
            String[] dayNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            int todayOfWeek = today.getDayOfWeek().getValue();
            
            for (int i = 0; i < 7; i++) {
                LocalDate date = today.minusDays(todayOfWeek - 1 - i);
                LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
                
                Long count = travelRecordMapper.selectCount(
                    new LambdaQueryWrapper<TravelRecord>()
                        .ge(TravelRecord::getCreatedAt, dayStart)
                        .le(TravelRecord::getCreatedAt, dayEnd)
                );
                
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("label", dayNames[i]);
                dayData.put("value", count != null ? count : 0);
                result.add(dayData);
            }
        } else {
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
                
                Long count = travelRecordMapper.selectCount(
                    new LambdaQueryWrapper<TravelRecord>()
                        .ge(TravelRecord::getCreatedAt, dayStart)
                        .le(TravelRecord::getCreatedAt, dayEnd)
                );
                
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("label", date.getMonthValue() + "/" + date.getDayOfMonth());
                dayData.put("value", count != null ? count : 0);
                result.add(dayData);
            }
        }
        
        return ApiResponse.success(result);
    }

    @GetMapping("/users")
    public ApiResponse<?> users(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Integer status) {
        var wrapper = new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(User::getNickname, keyword)
                .or().like(User::getPhone, keyword)
                .or().like(User::getRealName, keyword)
                .or().like(User::getEmail, keyword)
            );
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        var result = userMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(u -> {
            u.setPassword(null);
            CarbonPoints cp = carbonPointsMapper.selectOne(
                new LambdaQueryWrapper<CarbonPoints>().eq(CarbonPoints::getUserId, u.getId()));
            if (cp != null) {
                u.setPoints(cp.getAvailablePoints());
                u.setTotalCarbon(cp.getTotalCarbon());
            }
            // 添加额外统计
            Long travelCount = travelRecordMapper.selectCount(
                new LambdaQueryWrapper<TravelRecord>().eq(TravelRecord::getUserId, u.getId()));
            Long activityCount = activityParticipationMapper.selectCount(
                new LambdaQueryWrapper<ActivityParticipation>().eq(ActivityParticipation::getUserId, u.getId()));
            Long orderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getUserId, u.getId()));
            // 存储到User对象的扩展字段
            u.setPoints(cp != null ? cp.getAvailablePoints() : null);
            u.setTotalCarbon(cp != null ? cp.getTotalCarbon() : null);
        });
        return ApiResponse.success(result);
    }

    @PostMapping("/users")
    public ApiResponse<?> createUser(@RequestBody User user) {
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, user.getPhone())) != null) {
            return ApiResponse.error("手机号已存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        user.setStatus(1);
        user.setRole("user");
        userMapper.insert(user);
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) return ApiResponse.error("用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
        return ApiResponse.success("操作成功", null);
    }

    @PutMapping("/users/{id}")
    public ApiResponse<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = userMapper.selectById(id);
        if (user == null) return ApiResponse.error("用户不存在");
        
        if (body.containsKey("nickname")) {
            user.setNickname((String) body.get("nickname"));
        }
        if (body.containsKey("realName")) {
            user.setRealName((String) body.get("realName"));
        }
        if (body.containsKey("idCard")) {
            user.setIdCard((String) body.get("idCard"));
        }
        if (body.containsKey("email")) {
            user.setEmail((String) body.get("email"));
        }
        if (body.containsKey("avatar")) {
            user.setAvatar((String) body.get("avatar"));
        }
        if (body.containsKey("status")) {
            user.setStatus((Integer) body.get("status"));
        }
        if (body.containsKey("role")) {
            user.setRole((String) body.get("role"));
        }
        
        userMapper.updateById(user);
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @PutMapping("/users/{id}/reset-password")
    public ApiResponse<?> resetPassword(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return ApiResponse.error("用户不存在");
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
        return ApiResponse.success("密码已重置为：123456", null);
    }

    @GetMapping("/users/{id}")
    public ApiResponse<?> getUser(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return ApiResponse.error("用户不存在");
        user.setPassword(null);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("phone", user.getPhone());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("realName", user.getRealName());
        result.put("idCard", user.getIdCard());
        result.put("email", user.getEmail());
        result.put("status", user.getStatus());
        result.put("role", user.getRole());
        result.put("createdAt", user.getCreatedAt());
        result.put("updatedAt", user.getUpdatedAt());
        
        // 碳积分信息
        CarbonPoints cp = carbonPointsMapper.selectOne(
            new LambdaQueryWrapper<CarbonPoints>().eq(CarbonPoints::getUserId, id));
        if (cp != null) {
            result.put("points", cp.getAvailablePoints());
            result.put("totalPoints", cp.getTotalPoints());
            result.put("usedPoints", cp.getUsedPoints());
            result.put("totalCarbon", cp.getTotalCarbon());
        } else {
            result.put("points", BigDecimal.ZERO);
            result.put("totalPoints", BigDecimal.ZERO);
            result.put("usedPoints", BigDecimal.ZERO);
            result.put("totalCarbon", BigDecimal.ZERO);
        }
        
        // 出行统计
        Long travelCount = travelRecordMapper.selectCount(
            new LambdaQueryWrapper<TravelRecord>().eq(TravelRecord::getUserId, id));
        result.put("travelCount", travelCount);
        
        Long approvedCount = travelRecordMapper.selectCount(
            new LambdaQueryWrapper<TravelRecord>()
                .eq(TravelRecord::getUserId, id)
                .eq(TravelRecord::getStatus, 1));
        result.put("approvedTravelCount", approvedCount);
        
        // 活动参与数
        Long activityCount = activityParticipationMapper.selectCount(
            new LambdaQueryWrapper<ActivityParticipation>().eq(ActivityParticipation::getUserId, id));
        result.put("activityCount", activityCount);
        
        // 兑换订单数
        Long orderCount = orderMapper.selectCount(
            new LambdaQueryWrapper<Order>().eq(Order::getUserId, id));
        result.put("orderCount", orderCount);
        
        // 发帖数
        Long postCount = forumPostMapper.selectCount(
            new LambdaQueryWrapper<ForumPost>().eq(ForumPost::getUserId, id));
        result.put("postCount", postCount);
        
        // 用户排名
        Long rank = carbonPointsMapper.selectRank(id);
        result.put("rank", rank != null ? rank : 0);
        
        return ApiResponse.success(result);
    }

    @GetMapping("/profile")
    public ApiResponse<?> getProfile(jakarta.servlet.http.HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.success(getDefaultAdmin());
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.success(getDefaultAdmin());
        }
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @PutMapping("/profile")
    public ApiResponse<?> updateProfile(@RequestBody User user, jakarta.servlet.http.HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error("请先登录");
        }
        User existing = userMapper.selectById(userId);
        if (existing == null) {
            return ApiResponse.error("用户不存在");
        }
        existing.setNickname(user.getNickname());
        existing.setAvatar(user.getAvatar());
        userMapper.updateById(existing);
        existing.setPassword(null);
        return ApiResponse.success(existing);
    }

    @PutMapping("/change-password")
    public ApiResponse<?> changePassword(@RequestBody Map<String, String> body, 
                                          jakarta.servlet.http.HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error("请先登录");
        }
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ApiResponse.error("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return ApiResponse.success("密码修改成功", null);
    }

    private Map<String, Object> getDefaultAdmin() {
        Map<String, Object> admin = new HashMap<>();
        admin.put("id", 1);
        admin.put("nickname", "管理员");
        admin.put("phone", "138****8888");
        admin.put("avatar", "");
        admin.put("role", "admin");
        return admin;
    }

    @PostMapping("/announcements")
    public ApiResponse<?> createAnnouncement(@RequestBody Announcement announcement) {
        announcement.setStatus(1);
        announcementMapper.insert(announcement);
        return ApiResponse.success(announcement);
    }

    @GetMapping("/announcements")
    public ApiResponse<?> announcements(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        var result = announcementMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreatedAt)
        );
        return ApiResponse.success(result);
    }

    @DeleteMapping("/announcements/{id}")
    public ApiResponse<?> deleteAnnouncement(@PathVariable Long id) {
        announcementMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/announcements/{id}")
    public ApiResponse<?> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        Announcement existing = announcementMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.error("公告不存在");
        }
        existing.setTitle(announcement.getTitle());
        existing.setContent(announcement.getContent());
        announcementMapper.updateById(existing);
        return ApiResponse.success(existing);
    }

    @PostMapping("/products")
    public ApiResponse<?> createProduct(@RequestBody Product product) {
        product.setStatus(1);
        productMapper.insert(product);
        return ApiResponse.success(product);
    }

    @GetMapping("/products")
    public ApiResponse<?> getProducts(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "50") int size) {
        var result = productMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Product>().orderByDesc(Product::getId)
        );
        return ApiResponse.success(result.getRecords());
    }

    @PutMapping("/products/{id}")
    public ApiResponse<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productMapper.updateById(product);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/products/{id}")
    public ApiResponse<?> deleteProduct(@PathVariable Long id) {
        Long orderCount = orderMapper.selectCount(
            new LambdaQueryWrapper<Order>().eq(Order::getProductId, id)
        );
        if (orderCount > 0) {
            return ApiResponse.error("该商品存在关联订单，无法删除");
        }
        productMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    // 商品分类管理
    @GetMapping("/product-categories")
    public ApiResponse<?> getProductCategories() {
        var result = productCategoryMapper.selectList(
            new LambdaQueryWrapper<ProductCategory>().orderByAsc(ProductCategory::getSortOrder)
        );
        return ApiResponse.success(result);
    }

    @PostMapping("/product-categories")
    public ApiResponse<?> createProductCategory(@RequestBody ProductCategory category) {
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(99);
        }
        // 检查编码是否重复
        Long count = productCategoryMapper.selectCount(
            new LambdaQueryWrapper<ProductCategory>().eq(ProductCategory::getCode, category.getCode())
        );
        if (count > 0) {
            return ApiResponse.error("分类编码已存在");
        }
        productCategoryMapper.insert(category);
        return ApiResponse.success(category);
    }

    @PutMapping("/product-categories/{id}")
    public ApiResponse<?> updateProductCategory(@PathVariable Long id, @RequestBody ProductCategory category) {
        ProductCategory existing = productCategoryMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.error("分类不存在");
        }
        category.setId(id);
        productCategoryMapper.updateById(category);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/product-categories/{id}")
    public ApiResponse<?> deleteProductCategory(@PathVariable Long id) {
        // 检查是否有商品使用该分类
        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            return ApiResponse.error("分类不存在");
        }
        Long productCount = productMapper.selectCount(
            new LambdaQueryWrapper<Product>().eq(Product::getCategory, category.getCode())
        );
        if (productCount > 0) {
            return ApiResponse.error("该分类下有商品，无法删除");
        }
        productCategoryMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/activities")
    public ApiResponse<?> createActivity(@RequestBody Activity activity) {
        if (activity.getStatus() == null) {
            activity.setStatus(1);
        }
        activityMapper.insert(activity);
        return ApiResponse.success(activity);
    }

    @GetMapping("/activities")
    public ApiResponse<?> getActivities(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        var result = activityMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Activity>().orderByDesc(Activity::getId)
        );
        return ApiResponse.success(result);
    }

    @PutMapping("/activities/{id}")
    public ApiResponse<?> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        activity.setId(id);
        activityMapper.updateById(activity);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/activities/{id}")
    public ApiResponse<?> deleteActivity(@PathVariable Long id) {
        activityParticipationMapper.delete(
            new LambdaQueryWrapper<ActivityParticipation>()
                .eq(ActivityParticipation::getActivityId, id)
        );
        activityMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/activities/participations")
    public ApiResponse<?> getActivityParticipations(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) Long activityId) {
        var wrapper = new LambdaQueryWrapper<ActivityParticipation>()
            .orderByDesc(ActivityParticipation::getCreatedAt);
        if (activityId != null) {
            wrapper.eq(ActivityParticipation::getActivityId, activityId);
        }
        var result = activityParticipationMapper.selectPage(
            new Page<>(page, size),
            wrapper
        );
        
        result.getRecords().forEach(p -> {
            User user = userMapper.selectById(p.getUserId());
            if (user != null) {
                p.setUserName(user.getNickname());
                p.setAvatar(user.getAvatar());
            }
            Activity activity = activityMapper.selectById(p.getActivityId());
            if (activity != null) {
                p.setActivityTitle(activity.getTitle());
            }
        });
        
        return ApiResponse.success(result);
    }

    @PostMapping("/activities/participations/{id}/review")
    public ApiResponse<?> reviewParticipation(@PathVariable Long id,
                                               @RequestParam Integer status,
                                               @RequestParam(required = false) String comment) {
        ActivityParticipation p = activityParticipationMapper.selectById(id);
        if (p == null) return ApiResponse.error("参与记录不存在");
        p.setStatus(status);
        p.setReviewComment(comment);
        activityParticipationMapper.updateById(p);
        return ApiResponse.success("审核完成", null);
    }

    @GetMapping("/orders")
    public ApiResponse<?> getOrders(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) Integer status) {
        var wrapper = new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreatedAt);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        var result = orderMapper.selectPage(
            new Page<>(page, size),
            wrapper
        );
        
        result.getRecords().forEach(order -> {
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                order.setUserName(user.getNickname());
                order.setAvatar(user.getAvatar());
                order.setPhone(user.getPhone());
            }
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                order.setProductName(product.getName());
            }
        });
        
        return ApiResponse.success(result);
    }

    @PutMapping("/orders/{id}/status")
    public ApiResponse<?> updateOrderStatus(@PathVariable Long id, 
                                           @RequestParam Integer status,
                                           @RequestParam(required = false) String trackingNo) {
        Order order = orderMapper.selectById(id);
        if (order == null) return ApiResponse.error("订单不存在");
        
        int oldStatus = order.getStatus() != null ? order.getStatus() : 0;
        order.setStatus(status);
        if (trackingNo != null && !trackingNo.isEmpty()) {
            order.setDeliveryNo(trackingNo);
        }
        orderMapper.updateById(order);
        
        if (status != oldStatus) {
            String statusText = getStatusText(status);
            Message msg = new Message();
            msg.setSenderId(1L);
            msg.setReceiverId(order.getUserId());
            msg.setTitle("订单状态更新");
            String content;
            switch (status) {
                case 1: content = "您的订单已发货，请注意查收"; break;
                case 2: content = "您的订单已完成"; break;
                case 3: content = "您的订单已拒绝"; break;
                default: content = "您的订单待处理"; break;
            }
            msg.setContent("订单 " + order.getOrderNo() + " " + content);
            msg.setType("order");
            msg.setIsRead(0);
            messageMapper.insert(msg);
        }
        
        return ApiResponse.success("状态更新成功", null);
    }
    
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待处理";
            case 1: return "已发货";
            case 2: return "已完成";
            case 3: return "已取消";
            default: return "待处理";
        }
    }

    // 积分规则管理
    @GetMapping("/points-rules")
    public ApiResponse<?> getPointsRules() {
        var travelModes = travelModeMapper.selectList(
            new LambdaQueryWrapper<TravelMode>()
                .orderByAsc(TravelMode::getSortOrder)
        );
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (TravelMode mode : travelModes) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", mode.getId());
            item.put("travelModeId", mode.getId());
            item.put("modeName", mode.getName());
            item.put("name", mode.getName());
            item.put("icon", mode.getIcon());
            item.put("pointsPerKm", mode.getPointsPerKm());
            item.put("carbonReduction", mode.getCarbonReduction());
            item.put("enabled", mode.getStatus() == 1);
            item.put("weight", mode.getCarbonReduction() != null && mode.getCarbonReduction().compareTo(BigDecimal.ZERO) > 0 ? "high" : "medium");
            item.put("maxPerTrip", 100);
            item.put("sortOrder", mode.getSortOrder());
            item.put("status", mode.getStatus());
            result.add(item);
        }
        
        return ApiResponse.success(result);
    }

    @PostMapping("/points-rules")
    public ApiResponse<?> createPointsRule(@RequestBody TravelMode travelMode) {
        travelMode.setStatus(1);
        if (travelMode.getSortOrder() == null) {
            travelMode.setSortOrder(99);
        }
        travelModeMapper.insert(travelMode);
        return ApiResponse.success(travelMode);
    }

    @PutMapping("/points-rules/{id}")
    public ApiResponse<?> updatePointsRule(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        TravelMode mode = travelModeMapper.selectById(id);
        if (mode == null) {
            return ApiResponse.error("出行方式不存在");
        }
        
        if (body.containsKey("pointsPerKm")) {
            mode.setPointsPerKm(new BigDecimal(body.get("pointsPerKm").toString()));
        }
        if (body.containsKey("carbonReduction")) {
            mode.setCarbonReduction(new BigDecimal(body.get("carbonReduction").toString()));
        }
        if (body.containsKey("maxPointsPerTrip")) {
            mode.setMaxPointsPerTrip(new BigDecimal(body.get("maxPointsPerTrip").toString()));
        }
        if (body.containsKey("enabled")) {
            mode.setStatus((Boolean) body.get("enabled") ? 1 : 0);
        }
        travelModeMapper.updateById(mode);
        
        return ApiResponse.success("更新成功", null);
    }

    // 论坛管理
    @GetMapping("/forum/posts")
    public ApiResponse<?> getForumPosts(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) Integer status) {
        var wrapper = new LambdaQueryWrapper<ForumPost>().orderByDesc(ForumPost::getCreatedAt);
        if (status != null) {
            wrapper.eq(ForumPost::getStatus, status);
        }
        var result = forumPostMapper.selectPage(new Page<>(page, size), wrapper);
        // 填充作者信息
        for (ForumPost post : result.getRecords()) {
            if (post.getUserId() != null) {
                User user = userMapper.selectById(post.getUserId());
                if (user != null) {
                    post.setAuthorName(user.getNickname());
                    post.setAuthorAvatar(user.getAvatar());
                }
            }
        }
        return ApiResponse.success(result);
    }

    @GetMapping("/forum/posts/{id}")
    public ApiResponse<?> getForumPostDetail(@PathVariable Long id) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            return ApiResponse.error("帖子不存在");
        }
        if (post.getUserId() != null) {
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                post.setAuthorName(user.getNickname());
                post.setAuthorAvatar(user.getAvatar());
            }
        }
        return ApiResponse.success(post);
    }

    @PutMapping("/forum/posts/{id}/top")
    public ApiResponse<?> togglePostTop(@PathVariable Long id) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) return ApiResponse.error("帖子不存在");
        post.setIsTop(post.getIsTop() == 1 ? 0 : 1);
        forumPostMapper.updateById(post);
        return ApiResponse.success(post.getIsTop() == 1 ? "已置顶" : "已取消置顶", null);
    }

    @PutMapping("/forum/posts/{id}/hide")
    public ApiResponse<?> togglePostHide(@PathVariable Long id) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) return ApiResponse.error("帖子不存在");
        post.setStatus(post.getStatus() == 0 ? 1 : 0);
        forumPostMapper.updateById(post);
        return ApiResponse.success(post.getStatus() == 1 ? "已显示" : "已隐藏", null);
    }

    @DeleteMapping("/forum/posts/{id}")
    public ApiResponse<?> deletePost(@PathVariable Long id) {
        forumCommentMapper.delete(
            new LambdaQueryWrapper<ForumComment>()
                .eq(ForumComment::getPostId, id)
        );
        forumPostMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    // 帖子审核
    @PostMapping("/forum/posts/{id}/review")
    public ApiResponse<?> reviewPost(@PathVariable Long id,
                                      @RequestParam Integer status,
                                      @RequestParam(required = false) String comment,
                                      HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        forumService.reviewPost(id, status, comment, adminId);
        return ApiResponse.success(status == 1 ? "审核通过" : "审核驳回", null);
    }

    // 获取待审核帖子
    @GetMapping("/forum/posts/pending")
    public ApiResponse<?> getPendingPosts(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(forumService.getPendingPosts(page, size));
    }

    @GetMapping("/forum/comments/reported")
    public ApiResponse<?> getReportedComments(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        var result = forumCommentMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<ForumComment>()
                .gt(ForumComment::getReportCount, 0)
                .orderByDesc(ForumComment::getReportCount)
        );
        // 填充评论用户信息
        for (ForumComment comment : result.getRecords()) {
            if (comment.getUserId() != null) {
                User user = userMapper.selectById(comment.getUserId());
                if (user != null) {
                    comment.setUserName(user.getNickname());
                    comment.setUserAvatar(user.getAvatar());
                }
            }
        }
        return ApiResponse.success(result);
    }

    @PutMapping("/forum/comments/{id}/ignore")
    public ApiResponse<?> ignoreCommentReport(@PathVariable Long id) {
        ForumComment comment = forumCommentMapper.selectById(id);
        if (comment == null) return ApiResponse.error("评论不存在");
        comment.setReportCount(0);
        comment.setReportReason(null);
        forumCommentMapper.updateById(comment);
        return ApiResponse.success("已忽略举报", null);
    }

    @DeleteMapping("/forum/comments/{id}")
    public ApiResponse<?> deleteComment(@PathVariable Long id) {
        forumCommentMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    // 防作弊配置接口
    @GetMapping("/system-config")
    public ApiResponse<?> getSystemConfig() {
        List<SystemConfig> configs = systemConfigMapper.selectList(null);
        Map<String, String> result = new HashMap<>();
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return ApiResponse.success(result);
    }

    @PutMapping("/system-config")
    public ApiResponse<?> updateSystemConfig(@RequestBody Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, entry.getKey());
            SystemConfig config = systemConfigMapper.selectOne(wrapper);
            if (config != null) {
                config.setConfigValue(entry.getValue());
                systemConfigMapper.updateById(config);
            }
        }
        return ApiResponse.success("配置更新成功", null);
    }
}
