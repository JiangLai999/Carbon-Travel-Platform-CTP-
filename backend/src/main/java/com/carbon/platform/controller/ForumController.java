package com.carbon.platform.controller;

import com.carbon.platform.dto.ApiResponse;
import com.carbon.platform.entity.ForumPost;
import com.carbon.platform.service.ForumService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    @GetMapping("/sections")
    public ApiResponse<?> sections() {
        return ApiResponse.success(forumService.getSections());
    }

    @GetMapping("/posts")
    public ApiResponse<?> posts(@RequestParam(required = false) Long sectionId,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(forumService.getPosts(sectionId, keyword, page, size));
    }

    @GetMapping("/posts/{id}")
    public ApiResponse<?> postDetail(@PathVariable Long id) {
        return ApiResponse.success(forumService.getPostDetail(id));
    }

    @PostMapping("/posts")
    public ApiResponse<?> createPost(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long sectionId = Long.valueOf(body.get("sectionId").toString());
        String title = body.get("title").toString();
        String content = body.get("content").toString();
        String images = body.containsKey("images") ? body.get("images").toString() : null;
        return ApiResponse.success(forumService.createPost(userId, sectionId, title, content, images));
    }

    @PostMapping("/posts/{id}/comment")
    public ApiResponse<?> comment(@PathVariable Long id,
                                   @RequestBody Map<String, String> body,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(forumService.addComment(userId, id, body.get("content")));
    }

    @PostMapping("/posts/{id}/like")
    public ApiResponse<?> like(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        forumService.likePost(id, userId);
        return ApiResponse.success("点赞成功", null);
    }

    @DeleteMapping("/posts/{id}")
    public ApiResponse<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        forumService.deletePost(id, userId);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/posts/{id}/comments")
    public ApiResponse<?> getComments(@PathVariable Long id,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(forumService.getComments(id, page, size));
    }

    @GetMapping("/my-posts")
    public ApiResponse<?> myPosts(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(forumService.getUserPosts(userId, page, size));
    }
}
