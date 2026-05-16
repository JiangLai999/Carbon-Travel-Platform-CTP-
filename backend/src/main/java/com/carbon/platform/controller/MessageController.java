package com.carbon.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbon.platform.dto.ApiResponse;
import com.carbon.platform.entity.Message;
import com.carbon.platform.mapper.MessageMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageMapper messageMapper;

    @GetMapping
    public ApiResponse<?> list(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) Integer isRead,
                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
            .eq(Message::getReceiverId, userId)
            .orderByDesc(Message::getCreatedAt);

        if (isRead != null) {
            wrapper.eq(Message::getIsRead, isRead);
        }

        Page<Message> result = messageMapper.selectPage(
            new Page<>(page, size),
            wrapper
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/unread-count")
    public ApiResponse<?> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long count = messageMapper.selectCount(
            new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0)
        );
        return ApiResponse.success(count);
    }

    @PutMapping("/{id}/read")
    public ApiResponse<?> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Message msg = messageMapper.selectById(id);
        if (msg == null || !msg.getReceiverId().equals(userId)) {
            return ApiResponse.error("消息不存在");
        }
        msg.setIsRead(1);
        messageMapper.updateById(msg);
        return ApiResponse.success("标记成功", null);
    }

    @PutMapping("/read-all")
    public ApiResponse<?> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Message msg = new Message();
        msg.setIsRead(1);
        messageMapper.update(msg,
            new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0)
        );
        return ApiResponse.success("全部标记成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Message msg = messageMapper.selectById(id);
        if (msg == null || !msg.getReceiverId().equals(userId)) {
            return ApiResponse.error("消息不存在");
        }
        messageMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}
