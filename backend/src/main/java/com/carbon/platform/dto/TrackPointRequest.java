package com.carbon.platform.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class TrackPointRequest {
    private Long travelModeId;
    private String startLocation;
    private String endLocation;
    private List<TrackPoint> points;
    private BigDecimal totalDistance;
    private Long duration;

    @Data
    public static class TrackPoint {
        private BigDecimal latitude;
        private BigDecimal longitude;
        private BigDecimal altitude;
        private BigDecimal speed;
        private Integer accuracy;
        private Long timestamp;
    }
}
