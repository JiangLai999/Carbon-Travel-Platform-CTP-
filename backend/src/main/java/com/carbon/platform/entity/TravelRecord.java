package com.carbon.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("travel_records")
public class TravelRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long travelModeId;
    private String startLocation;
    private String endLocation;
    private BigDecimal distance;
    private BigDecimal carbonReduction;
    private BigDecimal pointsEarned;
    private Integer status;
    private String reviewComment;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDate travelDate;
    @TableField(exist = false)
    private String trackPoints;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
