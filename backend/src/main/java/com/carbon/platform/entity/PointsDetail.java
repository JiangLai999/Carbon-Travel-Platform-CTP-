package com.carbon.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("points_details")
public class PointsDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal points;
    private String type;
    private Long sourceId;
    private String description;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
