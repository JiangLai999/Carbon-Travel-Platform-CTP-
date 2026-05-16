package com.carbon.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbon.platform.entity.*;
import com.carbon.platform.mapper.*;
import com.carbon.platform.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelService {

    private final TravelRecordMapper travelRecordMapper;
    private final TravelModeMapper travelModeMapper;
    private final CarbonPointsMapper carbonPointsMapper;
    private final PointsDetailMapper pointsDetailMapper;
    private final TrackPointMapper trackPointMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final SystemConfigMapper systemConfigMapper;

    @Transactional
    public TravelRecord submitRecord(Long userId, TravelRecordRequest req) {
        TravelMode mode = travelModeMapper.selectById(req.getTravelModeId());
        if (mode == null) throw new RuntimeException("出行方式不存在");

        BigDecimal carbonReduction = mode.getCarbonReduction().multiply(req.getDistance());

        TravelRecord record = new TravelRecord();
        record.setUserId(userId);
        record.setTravelModeId(req.getTravelModeId());
        record.setStartLocation(req.getStartLocation());
        record.setEndLocation(req.getEndLocation());
        record.setDistance(req.getDistance());
        record.setCarbonReduction(carbonReduction);
        record.setPointsEarned(BigDecimal.ZERO);
        record.setStatus(0);
        record.setTravelDate(LocalDate.now());
        travelRecordMapper.insert(record);

        // 通知管理员有新的出行记录待审核
        notifyAdmins("新的出行记录待审核", "用户提交了 " + mode.getName() + " 出行记录，距离 " + req.getDistance() + " km");

        return record;
    }

    // 通知所有管理员
    private void notifyAdmins(String title, String content) {
        List<User> admins = userMapper.selectList(
            new LambdaQueryWrapper<User>().eq(User::getRole, "admin")
        );
        for (User admin : admins) {
            Message msg = new Message();
            msg.setSenderId(admin.getId());  // 使用管理员自己的ID作为sender
            msg.setReceiverId(admin.getId());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setType("system");
            msg.setIsRead(0);
            messageMapper.insert(msg);
        }
    }

    @Transactional
    public void reviewRecord(Long recordId, Integer status, String comment, Long adminId) {
        TravelRecord record = travelRecordMapper.selectById(recordId);
        if (record == null) throw new RuntimeException("记录不存在");

        record.setStatus(status);
        record.setReviewComment(comment);
        record.setReviewedBy(adminId);
        record.setReviewedAt(LocalDateTime.now());

        if (status == 1) {
            TravelMode mode = travelModeMapper.selectById(record.getTravelModeId());
            
            // 防作弊检查
            BigDecimal maxPointsPerTrip = mode.getMaxPointsPerTrip() != null ? mode.getMaxPointsPerTrip() : new BigDecimal("100");
            BigDecimal rawPoints = mode.getPointsPerKm().multiply(record.getDistance());
            BigDecimal points = rawPoints.compareTo(maxPointsPerTrip) > 0 ? maxPointsPerTrip : rawPoints;
            
            // 每日上限检查
            BigDecimal dailyLimit = getDailyLimit();
            BigDecimal todayPoints = getTodayPoints(record.getUserId());
            if (todayPoints.add(points).compareTo(dailyLimit) > 0) {
                points = dailyLimit.subtract(todayPoints);
                if (points.compareTo(BigDecimal.ZERO) < 0) points = BigDecimal.ZERO;
            }
            
            record.setPointsEarned(points);

            CarbonPoints cp = carbonPointsMapper.selectOne(
                new LambdaQueryWrapper<CarbonPoints>().eq(CarbonPoints::getUserId, record.getUserId())
            );
            if (cp != null) {
                cp.setTotalPoints(cp.getTotalPoints().add(points));
                cp.setAvailablePoints(cp.getAvailablePoints().add(points));
                cp.setTotalCarbon(cp.getTotalCarbon() != null ? cp.getTotalCarbon().add(record.getCarbonReduction()) : record.getCarbonReduction());
                carbonPointsMapper.updateById(cp);
            }

            PointsDetail detail = new PointsDetail();
            detail.setUserId(record.getUserId());
            detail.setPoints(points);
            detail.setType("travel");
            detail.setSourceId(recordId);
            detail.setDescription("出行记录审核通过，获得积分");
            pointsDetailMapper.insert(detail);

            Message msg = new Message();
            msg.setSenderId(record.getUserId());
            msg.setReceiverId(record.getUserId());
            msg.setContent("您的出行记录已审核通过，获得 " + points + " 积分");
            msg.setIsRead(0);
            messageMapper.insert(msg);
        } else if (status == 2) {
            Message msg = new Message();
            msg.setSenderId(record.getUserId());
            msg.setReceiverId(record.getUserId());
            msg.setContent("您的出行记录未通过审核。原因：" + (comment != null ? comment : "不符合要求"));
            msg.setIsRead(0);
            messageMapper.insert(msg);
        }
        travelRecordMapper.updateById(record);
    }
    
    private BigDecimal getDailyLimit() {
        SystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "daily_limit")
        );
        if (config != null && config.getConfigValue() != null) {
            return new BigDecimal(config.getConfigValue());
        }
        return new BigDecimal("500");
    }
    
    private BigDecimal getTodayPoints(Long userId) {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        List<PointsDetail> todayDetails = pointsDetailMapper.selectList(
            new LambdaQueryWrapper<PointsDetail>()
                .eq(PointsDetail::getUserId, userId)
                .eq(PointsDetail::getType, "travel")
                .ge(PointsDetail::getCreatedAt, todayStart)
        );
        return todayDetails.stream()
            .map(PointsDetail::getPoints)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Page<TravelRecord> getUserRecords(Long userId, int page, int size) {
        return travelRecordMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<TravelRecord>()
                .eq(TravelRecord::getUserId, userId)
                .orderByDesc(TravelRecord::getCreatedAt)
        );
    }

    public Page<TravelRecord> getUserRecords(Long userId, int page, int size, Integer status) {
        var wrapper = new LambdaQueryWrapper<TravelRecord>()
            .eq(TravelRecord::getUserId, userId)
            .orderByDesc(TravelRecord::getCreatedAt);
        if (status != null) {
            wrapper.eq(TravelRecord::getStatus, status);
        }
        return travelRecordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public TravelRecord getRecordDetail(Long id, Long userId) {
        TravelRecord record = travelRecordMapper.selectById(id);
        if (record == null) throw new RuntimeException("记录不存在");
        if (!record.getUserId().equals(userId)) throw new RuntimeException("无权查看");
        return record;
    }

    public Page<Map<String, Object>> getPendingRecords(int page, int size) {
        return getAdminRecords(page, size, 0, null, null, null);
    }

    public Page<Map<String, Object>> getAdminRecords(int page, int size, Integer status, Long travelModeId, String startDate, String endDate) {
        var wrapper = new LambdaQueryWrapper<TravelRecord>()
            .orderByDesc(TravelRecord::getCreatedAt);
        
        if (status != null) {
            wrapper.eq(TravelRecord::getStatus, status);
        }
        if (travelModeId != null) {
            wrapper.eq(TravelRecord::getTravelModeId, travelModeId);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(TravelRecord::getTravelDate, LocalDate.parse(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(TravelRecord::getTravelDate, LocalDate.parse(endDate));
        }

        Page<TravelRecord> recordPage = travelRecordMapper.selectPage(new Page<>(page, size), wrapper);
        
        List<Map<String, Object>> records = recordPage.getRecords().stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("userId", record.getUserId());
            map.put("travelModeId", record.getTravelModeId());
            map.put("startLocation", record.getStartLocation());
            map.put("endLocation", record.getEndLocation());
            map.put("distance", record.getDistance());
            map.put("carbonReduction", record.getCarbonReduction());
            map.put("pointsEarned", record.getPointsEarned());
            map.put("status", record.getStatus());
            map.put("reviewComment", record.getReviewComment());
            map.put("createdAt", record.getCreatedAt());
            map.put("travelDate", record.getTravelDate());

            User user = userMapper.selectById(record.getUserId());
            if (user != null) {
                map.put("userName", user.getNickname());
                map.put("nickname", user.getNickname());
                map.put("avatar", user.getAvatar());
                map.put("phone", user.getPhone());
            }

            TravelMode mode = travelModeMapper.selectById(record.getTravelModeId());
            if (mode != null) {
                map.put("travelMode", mode.getName());
                map.put("travelModeName", mode.getName());
            }

            return map;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> result = new Page<>(page, size);
        result.setRecords(records);
        result.setTotal(recordPage.getTotal());
        return result;
    }

    public Map<String, Object> getRecordStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total", travelRecordMapper.selectCount(null));
        stats.put("pending", travelRecordMapper.selectCount(
            new LambdaQueryWrapper<TravelRecord>().eq(TravelRecord::getStatus, 0)
        ));
        stats.put("approved", travelRecordMapper.selectCount(
            new LambdaQueryWrapper<TravelRecord>().eq(TravelRecord::getStatus, 1)
        ));
        stats.put("rejected", travelRecordMapper.selectCount(
            new LambdaQueryWrapper<TravelRecord>().eq(TravelRecord::getStatus, 2)
        ));
        
        return stats;
    }

    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        Long totalCount = travelRecordMapper.selectCount(
            new LambdaQueryWrapper<TravelRecord>().eq(TravelRecord::getUserId, userId)
        );
        
        Long approvedCount = travelRecordMapper.selectCount(
            new LambdaQueryWrapper<TravelRecord>()
                .eq(TravelRecord::getUserId, userId)
                .eq(TravelRecord::getStatus, 1)
        );
        
        Long pendingCount = travelRecordMapper.selectCount(
            new LambdaQueryWrapper<TravelRecord>()
                .eq(TravelRecord::getUserId, userId)
                .eq(TravelRecord::getStatus, 0)
        );
        
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        Long monthCount = travelRecordMapper.selectCount(
            new LambdaQueryWrapper<TravelRecord>()
                .eq(TravelRecord::getUserId, userId)
                .ge(TravelRecord::getCreatedAt, monthStart)
        );
        
        BigDecimal totalDistance = travelRecordMapper.selectTotalDistance(userId);
        if (totalDistance == null) totalDistance = BigDecimal.ZERO;
        
        stats.put("totalCount", totalCount);
        stats.put("approvedCount", approvedCount);
        stats.put("pendingCount", pendingCount);
        stats.put("monthCount", monthCount);
        stats.put("totalDistance", totalDistance);
        
        return stats;
    }

    @Transactional
    public TravelRecord submitTrack(Long userId, TrackPointRequest req) {
        TravelMode mode = travelModeMapper.selectById(req.getTravelModeId());
        if (mode == null) throw new RuntimeException("出行方式不存在");

        BigDecimal carbonReduction = mode.getCarbonReduction().multiply(req.getTotalDistance());

        TravelRecord record = new TravelRecord();
        record.setUserId(userId);
        record.setTravelModeId(req.getTravelModeId());
        record.setStartLocation(req.getStartLocation());
        record.setEndLocation(req.getEndLocation());
        record.setDistance(req.getTotalDistance());
        record.setCarbonReduction(carbonReduction);
        record.setPointsEarned(BigDecimal.ZERO);
        record.setStatus(0);
        record.setTravelDate(LocalDate.now());
        travelRecordMapper.insert(record);

        if (req.getPoints() != null && !req.getPoints().isEmpty()) {
            for (TrackPointRequest.TrackPoint point : req.getPoints()) {
                TrackPoint tp = new TrackPoint();
                tp.setUserId(userId);
                tp.setRecordId(record.getId());
                tp.setLatitude(point.getLatitude());
                tp.setLongitude(point.getLongitude());
                tp.setAltitude(point.getAltitude());
                tp.setSpeed(point.getSpeed());
                tp.setAccuracy(point.getAccuracy());
                tp.setTimestamp(LocalDateTime.now());
                trackPointMapper.insert(tp);
            }
        }

        return record;
    }
}
