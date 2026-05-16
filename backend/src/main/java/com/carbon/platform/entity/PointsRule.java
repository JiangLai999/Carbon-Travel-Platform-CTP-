package com.carbon.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("points_rules")
public class PointsRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long travelModeId;
    private BigDecimal pointsPerKm;
    private BigDecimal carbonReduction;
    private Integer status;
    private String description;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
