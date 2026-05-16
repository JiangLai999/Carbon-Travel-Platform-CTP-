package com.carbon.platform.controller;

import com.carbon.platform.dto.*;
import com.carbon.platform.service.TravelService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/travel")
@RequiredArgsConstructor
public class TravelController {

    private final TravelService travelService;

    @PostMapping("/record")
    public ApiResponse<?> submit(@RequestBody TravelRecordRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(travelService.submitRecord(userId, req));
    }

    @GetMapping("/records")
    public ApiResponse<?> myRecords(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) Integer status,
                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(travelService.getUserRecords(userId, page, size, status));
    }

    @GetMapping("/records/{id}")
    public ApiResponse<?> recordDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(travelService.getRecordDetail(id, userId));
    }

    @GetMapping("/pending")
    public ApiResponse<?> pending(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) Long travelModeId,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate) {
        return ApiResponse.success(travelService.getAdminRecords(page, size, status, travelModeId, startDate, endDate));
    }

    @GetMapping("/stats")
    public ApiResponse<?> stats() {
        return ApiResponse.success(travelService.getRecordStats());
    }

    @PostMapping("/review/{id}")
    public ApiResponse<?> review(@PathVariable Long id,
                                  @RequestParam Integer status,
                                  @RequestParam(required = false) String comment,
                                  HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        travelService.reviewRecord(id, status, comment, adminId);
        return ApiResponse.success("审核完成", null);
    }

    @PostMapping("/track")
    public ApiResponse<?> submitTrack(@RequestBody TrackPointRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(travelService.submitTrack(userId, req));
    }

    @GetMapping("/user-stats")
    public ApiResponse<?> myStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(travelService.getUserStats(userId));
    }
}
