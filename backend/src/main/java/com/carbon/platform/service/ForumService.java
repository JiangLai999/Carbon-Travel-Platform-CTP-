package com.carbon.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbon.platform.entity.*;
import com.carbon.platform.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumPostMapper postMapper;
    private final ForumCommentMapper commentMapper;
    private final ForumSectionMapper sectionMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final SensitiveWordService sensitiveWordService;

    public List<ForumSection> getSections() {
        return sectionMapper.selectList(
            new LambdaQueryWrapper<ForumSection>()
                .eq(ForumSection::getStatus, 1)
                .orderByAsc(ForumSection::getSortOrder)
        );
    }

    public Page<ForumPost> getPosts(Long sectionId, String keyword, int page, int size) {
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<ForumPost>()
            .eq(ForumPost::getStatus, 1)  // 只显示审核通过的帖子
            .orderByDesc(ForumPost::getIsTop)
            .orderByDesc(ForumPost::getCreatedAt);
        if (sectionId != null) wrapper.eq(ForumPost::getSectionId, sectionId);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ForumPost::getTitle, keyword).or().like(ForumPost::getContent, keyword);
        }
        Page<ForumPost> result = postMapper.selectPage(new Page<>(page, size), wrapper);
        // 填充用户名称
        fillPostAuthorInfo(result.getRecords());
        return result;
    }

    public ForumPost getPostDetail(Long id) {
        ForumPost post = postMapper.selectById(id);
        if (post == null) throw new RuntimeException("帖子不存在");
        // 增加浏览量
        post.setViews(post.getViews() + 1);
        postMapper.updateById(post);
        // 填充用户名称
        fillPostAuthorInfo(post);
        return post;
    }

    // 填充帖子作者信息
    private void fillPostAuthorInfo(List<ForumPost> posts) {
        for (ForumPost post : posts) {
            fillPostAuthorInfo(post);
        }
    }

    private void fillPostAuthorInfo(ForumPost post) {
        if (post.getUserId() != null) {
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                post.setAuthorName(user.getNickname());
                post.setAuthorAvatar(user.getAvatar());
            }
        }
    }

    @Transactional
    public ForumPost createPost(Long userId, Long sectionId, String title, String content, String images) {
        // 敏感词检查
        if (sensitiveWordService.containsSensitiveWord(title)) {
            throw new RuntimeException("标题包含违规内容，请修改后重新提交");
        }
        if (sensitiveWordService.containsSensitiveWord(content)) {
            throw new RuntimeException("内容包含违规内容，请修改后重新提交");
        }

        ForumPost post = new ForumPost();
        post.setSectionId(sectionId);
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        post.setImages(images);
        post.setViews(0);
        post.setLikes(0);
        post.setCommentsCount(0);
        post.setIsTop(0);
        post.setStatus(1);  // 自动审核通过
        postMapper.insert(post);

        // 通知管理员有新帖子发布
        notifyAdmins("新帖子发布", "用户发布了帖子「" + title + "」");

        return post;
    }

    @Transactional
    public ForumComment addComment(Long userId, Long postId, String content) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != 1) throw new RuntimeException("帖子不存在");

        // 敏感词检查
        if (sensitiveWordService.containsSensitiveWord(content)) {
            throw new RuntimeException("评论包含违规内容，请修改后重新提交");
        }

        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setLikes(0);
        comment.setStatus(1);
        commentMapper.insert(comment);

        post.setCommentsCount(post.getCommentsCount() + 1);
        postMapper.updateById(post);

        // 通知帖子作者有新评论
        if (!userId.equals(post.getUserId())) {
            User commenter = userMapper.selectById(userId);
            String commenterName = commenter != null ? commenter.getNickname() : "用户";
            sendNotification(post.getUserId(), "新评论通知", commenterName + " 评论了您的帖子「" + post.getTitle() + "」", "forum_comment");
        }

        return comment;
    }

    @Transactional
    public void likePost(Long postId, Long userId) {
        ForumPost post = postMapper.selectById(postId);
        if (post != null) {
            post.setLikes(post.getLikes() + 1);
            postMapper.updateById(post);

            // 通知帖子作者有人点赞
            if (!userId.equals(post.getUserId())) {
                User liker = userMapper.selectById(userId);
                String likerName = liker != null ? liker.getNickname() : "用户";
                sendNotification(post.getUserId(), "点赞通知", likerName + " 点赞了您的帖子「" + post.getTitle() + "」", "forum_like");
            }
        }
    }

    public void deletePost(Long postId, Long userId) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null) throw new RuntimeException("帖子不存在");
        if (!post.getUserId().equals(userId)) throw new RuntimeException("无权删除");
        post.setStatus(-1);  // 软删除
        postMapper.updateById(post);
    }

    public Page<ForumComment> getComments(Long postId, int page, int size) {
        Page<ForumComment> result = commentMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<ForumComment>()
                .eq(ForumComment::getPostId, postId)
                .eq(ForumComment::getStatus, 1)
                .orderByDesc(ForumComment::getCreatedAt)
        );
        // 填充用户信息
        fillCommentUserInfo(result.getRecords());
        return result;
    }

    // 填充评论用户信息
    private void fillCommentUserInfo(List<ForumComment> comments) {
        for (ForumComment comment : comments) {
            if (comment.getUserId() != null) {
                User user = userMapper.selectById(comment.getUserId());
                if (user != null) {
                    comment.setUserName(user.getNickname());
                    comment.setUserAvatar(user.getAvatar());
                }
            }
        }
    }

    public Page<ForumPost> getUserPosts(Long userId, int page, int size) {
        return postMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getUserId, userId)
                .ne(ForumPost::getStatus, -1)  // 排除已删除的
                .orderByDesc(ForumPost::getCreatedAt)
        );
    }

    // 审核帖子
    @Transactional
    public void reviewPost(Long postId, Integer status, String comment, Long adminId) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null) throw new RuntimeException("帖子不存在");

        post.setStatus(status);
        postMapper.updateById(post);

        // 通知作者审核结果
        if (status == 1) {
            sendNotification(post.getUserId(), "帖子审核通过", "您发布的帖子「" + post.getTitle() + "」已通过审核", "forum_approved");
        } else if (status == 2) {
            sendNotification(post.getUserId(), "帖子审核未通过", "您发布的帖子「" + post.getTitle() + "」未通过审核。原因：" + (comment != null ? comment : "不符合社区规范"), "forum_rejected");
        }
    }

    // 获取待审核帖子
    public Page<ForumPost> getPendingPosts(int page, int size) {
        return postMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, 0)
                .orderByAsc(ForumPost::getCreatedAt)
        );
    }

    // 通知管理员
    private void notifyAdmins(String title, String content) {
        List<User> admins = userMapper.selectList(
            new LambdaQueryWrapper<User>().eq(User::getRole, "admin")
        );
        for (User admin : admins) {
            Message msg = new Message();
            msg.setSenderId(admin.getId());
            msg.setReceiverId(admin.getId());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setType("system");
            msg.setIsRead(0);
            messageMapper.insert(msg);
        }
    }

    // 发送通知给用户
    private void sendNotification(Long receiverId, String title, String content, String type) {
        Message msg = new Message();
        msg.setSenderId(receiverId);
        msg.setReceiverId(receiverId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setType(type);
        msg.setIsRead(0);
        messageMapper.insert(msg);
    }
}
