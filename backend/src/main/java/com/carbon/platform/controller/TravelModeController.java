package com.carbon.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carbon.platform.dto.ApiResponse;
import com.carbon.platform.entity.TravelMode;
import com.carbon.platform.mapper.TravelModeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/travel")
@RequiredArgsConstructor
public class TravelModeController {

    private final TravelModeMapper travelModeMapper;

    @GetMapping("/modes")
    public ApiResponse<?> getModes() {
        List<TravelMode> modes = travelModeMapper.selectList(
            new LambdaQueryWrapper<TravelMode>()
                .eq(TravelMode::getStatus, 1)
                .orderByAsc(TravelMode::getSortOrder)
        );
        return ApiResponse.success(modes);
    }
}
