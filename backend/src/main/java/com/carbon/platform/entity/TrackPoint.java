package com.carbon.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("track_points")
public class TrackPoint {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long recordId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal altitude;
    private BigDecimal speed;
    private Integer accuracy;
    private LocalDateTime timestamp;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
