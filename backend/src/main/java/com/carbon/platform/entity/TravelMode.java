package com.carbon.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("travel_modes")
public class TravelMode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String icon;
    private BigDecimal carbonReduction;
    private BigDecimal pointsPerKm;
    private BigDecimal maxPointsPerTrip;
    private Integer sortOrder;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
