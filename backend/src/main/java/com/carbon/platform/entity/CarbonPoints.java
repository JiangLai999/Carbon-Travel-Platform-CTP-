package com.carbon.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("carbon_points")
public class CarbonPoints {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal totalPoints;
    private BigDecimal availablePoints;
    private BigDecimal usedPoints;
    private BigDecimal totalCarbon;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
