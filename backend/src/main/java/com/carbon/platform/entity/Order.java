package com.carbon.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal pointsSpent;
    private Integer status;
    private String deliveryAddress;
    private String deliveryNo;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private String phone;
    @TableField(exist = false)
    private String productName;
}
