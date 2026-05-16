package com.carbon.platform.controller;

import com.carbon.platform.dto.ApiResponse;
import com.carbon.platform.service.ActivityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ApiResponse<?> list(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(activityService.getActivities(page, size));
    }

    @PostMapping("/{id}/join")
    public ApiResponse<?> join(@PathVariable Long id,
                                @RequestParam(required = false) String evidenceUrl,
                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(activityService.join(userId, id, evidenceUrl));
    }

    // 更新参与凭证
    @PutMapping("/participation/{id}/evidence")
    public ApiResponse<?> updateEvidence(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String evidenceUrl = body.get("evidenceUrl");
        activityService.updateEvidence(id, userId, evidenceUrl);
        return ApiResponse.success("凭证更新成功", null);
    }

    @PostMapping("/participation/{id}/review")
    public ApiResponse<?> review(@PathVariable Long id,
                                  @RequestParam Integer status,
                                  @RequestParam(required = false) String comment,
                                  HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        activityService.reviewParticipation(id, status, comment, adminId);
        return ApiResponse.success("审核完成", null);
    }
}
