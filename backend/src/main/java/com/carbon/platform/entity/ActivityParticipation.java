package com.carbon.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity_participations")
public class ActivityParticipation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long userId;
    private String evidenceUrl;
    private Integer status;
    private String reviewComment;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private String activityTitle;
}
