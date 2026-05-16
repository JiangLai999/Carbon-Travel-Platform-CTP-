package com.carbon.platform.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TravelRecordResponse {
    private Long id;
    private String travelModeName;
    private String startLocation;
    private String endLocation;
    private BigDecimal distance;
    private BigDecimal carbonReduction;
    private BigDecimal pointsEarned;
    private Integer status;
    private String statusName;
}
