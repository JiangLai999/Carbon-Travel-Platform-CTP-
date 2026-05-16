package com.carbon.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String password;
    private String nickname;
    private String avatar;
    private String realName;
    private String idCard;
    private Integer status;
    private String role;
    private String email;
    private String deliveryAddress;
    private String deliveryName;
    private String deliveryPhone;
    private String province;
    private String city;
    private String district;
    @TableField(exist = false)
    private BigDecimal points;
    @TableField(exist = false)
    private BigDecimal totalCarbon;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
