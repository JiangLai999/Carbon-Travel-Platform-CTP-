package com.carbon.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("forum_comments")
public class ForumComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private Integer likes;
    private Integer status;
    private Integer reportCount;
    private String reportReason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // 非数据库字段，用于显示用户名称
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String userAvatar;
}
