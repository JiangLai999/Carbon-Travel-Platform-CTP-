package com.carbon.platform.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PointsRuleRequest {
    private Long travelModeId;
    private BigDecimal pointsPerKm;
    private BigDecimal carbonReduction;
    private String description;
}
