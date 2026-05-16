package com.carbon.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("activities")
public class Activity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String requirement;
    private BigDecimal rewardPoints;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
