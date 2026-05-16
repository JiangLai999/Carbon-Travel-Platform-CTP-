package com.carbon.platform.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TravelRecordRequest {
    private Long travelModeId;
    private String startLocation;
    private String endLocation;
    private BigDecimal distance;
}
