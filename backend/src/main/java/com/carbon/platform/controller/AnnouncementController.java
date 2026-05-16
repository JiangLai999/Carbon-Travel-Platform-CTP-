package com.carbon.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbon.platform.dto.ApiResponse;
import com.carbon.platform.entity.Announcement;
import com.carbon.platform.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementMapper announcementMapper;

    @GetMapping
    public ApiResponse<?> list(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        var result = announcementMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getCreatedAt)
        );
        return ApiResponse.success(result);
    }
}
