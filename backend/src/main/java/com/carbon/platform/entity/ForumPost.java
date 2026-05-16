package com.carbon.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("forum_posts")
public class ForumPost {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sectionId;
    private Long userId;
    private String title;
    private String content;
    private String images;
    private Integer views;
    private Integer likes;
    private Integer commentsCount;
    private Integer isTop;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // 非数据库字段，用于显示用户名称
    @TableField(exist = false)
    private String authorName;
    @TableField(exist = false)
    private String authorAvatar;
}
