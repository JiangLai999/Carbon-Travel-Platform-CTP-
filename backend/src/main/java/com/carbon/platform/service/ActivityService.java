package com.carbon.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbon.platform.entity.*;
import com.carbon.platform.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityParticipationMapper participationMapper;
    private final CarbonPointsMapper carbonPointsMapper;
    private final PointsDetailMapper pointsDetailMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    public Page<Activity> getActivities(int page, int size) {
        return activityMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, 1)
                .orderByDesc(Activity::getCreatedAt)
        );
    }

    @Transactional
    public ActivityParticipation join(Long userId, Long activityId, String evidenceUrl) {
        Long count = participationMapper.selectCount(
            new LambdaQueryWrapper<ActivityParticipation>()
                .eq(ActivityParticipation::getUserId, userId)
                .eq(ActivityParticipation::getActivityId, activityId)
        );
        if (count > 0) throw new RuntimeException("已报名该活动");

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) throw new RuntimeException("活动不存在");

        ActivityParticipation p = new ActivityParticipation();
        p.setActivityId(activityId);
        p.setUserId(userId);
        p.setEvidenceUrl(evidenceUrl);
        p.setStatus(0);
        participationMapper.insert(p);

        // 通知管理员有新的活动参与待审核
        notifyAdmins("新的活动参与待审核", "用户报名了活动「" + activity.getTitle() + "」");

        return p;
    }

    // 更新参与凭证
    @Transactional
    public void updateEvidence(Long participationId, Long userId, String evidenceUrl) {
        ActivityParticipation p = participationMapper.selectById(participationId);
        if (p == null) throw new RuntimeException("参与记录不存在");
        if (!p.getUserId().equals(userId)) throw new RuntimeException("无权修改");
        if (p.getStatus() != 0) throw new RuntimeException("该记录已审核，无法修改");

        p.setEvidenceUrl(evidenceUrl);
        participationMapper.updateById(p);
    }

    @Transactional
    public void reviewParticipation(Long participationId, Integer status, String comment, Long adminId) {
        ActivityParticipation p = participationMapper.selectById(participationId);
        if (p == null) throw new RuntimeException("参与记录不存在");

        p.setStatus(status);
        p.setReviewComment(comment);
        p.setReviewedBy(adminId);
        p.setReviewedAt(LocalDateTime.now());
        participationMapper.updateById(p);

        if (status == 1) {
            Activity activity = activityMapper.selectById(p.getActivityId());
            CarbonPoints cp = carbonPointsMapper.selectOne(
                new LambdaQueryWrapper<CarbonPoints>().eq(CarbonPoints::getUserId, p.getUserId())
            );
            cp.setTotalPoints(cp.getTotalPoints().add(activity.getRewardPoints()));
            cp.setAvailablePoints(cp.getAvailablePoints().add(activity.getRewardPoints()));
            carbonPointsMapper.updateById(cp);

            PointsDetail detail = new PointsDetail();
            detail.setUserId(p.getUserId());
            detail.setPoints(activity.getRewardPoints());
            detail.setType("activity");
            detail.setSourceId(p.getActivityId());
            detail.setDescription("参与活动：" + activity.getTitle());
            pointsDetailMapper.insert(detail);
        }
    }

    // 通知所有管理员
    private void notifyAdmins(String title, String content) {
        List<User> admins = userMapper.selectList(
            new LambdaQueryWrapper<User>().eq(User::getRole, "admin")
        );
        for (User admin : admins) {
            Message msg = new Message();
            msg.setSenderId(admin.getId());
            msg.setReceiverId(admin.getId());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setType("system");
            msg.setIsRead(0);
            messageMapper.insert(msg);
        }
    }
}
