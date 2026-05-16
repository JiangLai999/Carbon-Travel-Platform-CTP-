package com.carbon.platform.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carbon.platform.entity.*;
import com.carbon.platform.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserMapper userMapper;
    private final CarbonPointsMapper carbonPointsMapper;
    private final TravelModeMapper travelModeMapper;
    private final ProductMapper productMapper;
    private final ActivityMapper activityMapper;
    private final AnnouncementMapper announcementMapper;
    private final ForumSectionMapper forumSectionMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumCommentMapper forumCommentMapper;
    private final TravelRecordMapper travelRecordMapper;
    private final ActivityParticipationMapper activityParticipationMapper;
    private final OrderMapper orderMapper;
    private final PointsDetailMapper pointsDetailMapper;
    private final PointsRuleMapper pointsRuleMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            log.info("开始初始化测试数据...");
            createAdminIfNotExists("13800138000", "admin123", "系统管理员");
            initTravelModes();
            initTestUsers();
            initProducts();
            initActivities();
            initAnnouncements();
            initForumSections();
            initForumPosts();
            initTravelRecords();
            initActivityParticipations();
            initOrders();
            initPointsRules();
            initPointsDetails();
            log.info("测试数据初始化完成");
        } catch (Exception e) {
            log.error("数据初始化失败: {}", e.getMessage(), e);
        }
    }

    private void createAdminIfNotExists(String phone, String password, String nickname) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, phone)) > 0) return;
        
        User admin = new User();
        admin.setPhone(phone);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setNickname(nickname);
        admin.setStatus(1);
        admin.setRole("admin");
        userMapper.insert(admin);
        
        CarbonPoints points = new CarbonPoints();
        points.setUserId(admin.getId());
        points.setTotalPoints(BigDecimal.ZERO);
        points.setAvailablePoints(BigDecimal.ZERO);
        points.setUsedPoints(BigDecimal.ZERO);
        carbonPointsMapper.insert(points);
        
        log.info("管理员账号已创建: {} / {}", phone, password);
    }

    private void initTravelModes() {
        if (travelModeMapper.selectCount(null) > 0) return;
        
        travelModeMapper.insert(createTravelMode("步行", "location", BigDecimal.ZERO, "5", 1));
        travelModeMapper.insert(createTravelMode("骑行", "bicycle", "0.21", "10", 2));
        travelModeMapper.insert(createTravelMode("公交", "bus", "0.08", "8", 3));
        travelModeMapper.insert(createTravelMode("地铁", "metro", "0.04", "7", 4));
        travelModeMapper.insert(createTravelMode("电动车", "location", "0.05", "6", 5));
        
        log.info("出行方式数据已初始化（5条）");
    }
    
    private TravelMode createTravelMode(String name, String icon, BigDecimal carbonReduction, String points, int sortOrder) {
        TravelMode mode = new TravelMode();
        mode.setName(name);
        mode.setIcon(icon);
        mode.setCarbonReduction(carbonReduction);
        mode.setPointsPerKm(new BigDecimal(points));
        mode.setSortOrder(sortOrder);
        mode.setStatus(1);
        return mode;
    }
    
    private TravelMode createTravelMode(String name, String icon, String carbonReduction, String points, int sortOrder) {
        return createTravelMode(name, icon, new BigDecimal(carbonReduction), points, sortOrder);
    }

    private void initTestUsers() {
        String[] phones = {"13900000001", "13900000002", "13900000003", "13900000004", "13900000005"};
        String[] nicknames = {"环保达人小明", "绿色行者户外", "低碳先锋", "骑行爱好者", "城市通勤族"};
        String[] avatars = {
            "/uploads/avatars/avatar_1.jpg",
            "/uploads/avatars/avatar_2.jpg",
            "/uploads/avatars/avatar_3.jpg",
            "/uploads/avatars/avatar_4.jpg",
            "/uploads/avatars/avatar_5.jpg"
        };

        for (int i = 0; i < phones.length; i++) {
            if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, phones[i])) > 0) continue;
            
            User user = new User();
            user.setPhone(phones[i]);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setNickname(nicknames[i]);
            user.setAvatar(avatars[i]);
            user.setStatus(1);
            user.setRole("user");
            userMapper.insert(user);

            CarbonPoints points = new CarbonPoints();
            points.setUserId(user.getId());
            points.setTotalPoints(BigDecimal.ZERO);
            points.setAvailablePoints(BigDecimal.ZERO);
            points.setUsedPoints(BigDecimal.ZERO);
            points.setTotalCarbon(BigDecimal.ZERO);
            carbonPointsMapper.insert(points);
        }
        
        log.info("测试用户数据已初始化（5个）");
    }

    private void initProducts() {
        if (productMapper.selectCount(null) > 0) return;

        productMapper.insert(createProduct("折叠自行车", "轻便折叠，通勤首选，低碳出行好伴侣", 
            "/uploads/products/bike_folding.jpg", "bike", "500", 10, "低碳环保", 1));
        productMapper.insert(createProduct("山地自行车", "专业级山地骑行，坚固耐用",
            "/uploads/products/bike_mountain.jpg", "bike", "800", 5, "运动健身", 2));
        productMapper.insert(createProduct("智能骑行头盔", "骑行安全必备，内置LED灯，夜间骑行更安全",
            "/uploads/products/helmet.jpg", "equipment", "200", 20, "安全骑行", 3));
        productMapper.insert(createProduct("骑行手套", "减震防滑，透气舒适",
            "/uploads/products/gloves.jpg", "equipment", "80", 50, "舒适骑行", 4));
        productMapper.insert(createProduct("骑行背包", "防水透气，多功能收纳",
            "/uploads/products/backpack.jpg", "equipment", "120", 30, "实用便捷", 5));
        productMapper.insert(createProduct("防晒骑行服", "透气速干，UV防护，夏日骑行必备",
            "/uploads/products/gloves.jpg", "accessories", "150", 30, "环保材质", 6));
        productMapper.insert(createProduct("便携雨衣", "折叠收纳，随身携带，应对突发天气",
            "/uploads/products/raincoat.jpg", "accessories", "80", 50, "可回收材料", 7));
        productMapper.insert(createProduct("铝合金水壶架", "轻量化设计，通用型安装",
            "/uploads/products/helmet.jpg", "accessories", "60", 100, "铝合金环保", 8));
        productMapper.insert(createProduct("自行车车灯", "USB充电，强光远射，安全夜骑",
            "/uploads/products/helmet.jpg", "accessories", "90", 40, "夜间安全", 9));
        productMapper.insert(createProduct("便携打气筒", "小巧便携，随时补气",
            "/uploads/products/helmet.jpg", "accessories", "50", 60, "维护必备", 10));
        
        log.info("商品数据已初始化（10条）");
    }
    
    private Product createProduct(String name, String desc, String imageUrl, String category, 
            String points, int stock, String carbonLabel, int sortOrder) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setImageUrl(imageUrl);
        p.setCategory(category);
        p.setPointsRequired(new BigDecimal(points));
        p.setStock(stock);
        p.setCarbonLabel(carbonLabel);
        p.setStatus(1);
        p.setSortOrder(sortOrder);
        return p;
    }

    private void initActivities() {
        if (activityMapper.selectCount(null) > 0) return;

        Activity a1 = createActivity("绿色出行月", 
            "参与本月低碳出行挑战，累计骑行50km即可获得奖励！活动期间每天记录骑行数据，系统会自动计算您的碳减排量。完成挑战的用户将获得100积分和「低碳达人」称号。",
            "/uploads/activities/helmet.jpg",
            LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), "累计骑行50km", "100", "线下活动", 1);
        activityMapper.insert(a1);
        
        activityMapper.insert(createActivity("无车日挑战",
            "选择公共交通或自行车出行，记录一天的绿色出行。成功完成挑战的用户可获得50积分奖励。",
            "/uploads/activities/raincoat.jpg",
            LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 22), "记录一天出行", "50", "线上活动", 1));
        
        activityMapper.insert(createActivity("低碳知识竞赛",
            "参与低碳知识问答，答对8题以上获得30积分奖励。每位用户每天有3次答题机会，取最高分计入排名。",
            "/uploads/activities/knowledge_quiz.jpg",
            LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 20), "答对8题以上", "30", "线上工作坊", 1));
        
        activityMapper.insert(createActivity("周末骑行活动",
            "每周六组织城市骑行，路线经过精心设计，沿途设有补给点。完成全程可获得80积分。",
            "/uploads/activities/cycling_event.jpg",
            LocalDate.of(2026, 3, 7), LocalDate.of(2026, 12, 31), "完成全程约30km骑行", "80", "线下峰会", 1));
        
        activityMapper.insert(createActivity("春节绿色出行",
            "春节期间选择绿色出行方式，累计记录10次出行可获得新春礼包。",
            "/uploads/activities/spring_travel.jpg",
            LocalDate.of(2026, 1, 28), LocalDate.of(2026, 2, 15), "累计10次绿色出行", "150", "线下活动", 1));
        
        activityMapper.insert(createActivity("元宵节特别活动",
            "元宵节当天骑行赏灯，完成灯会路线打卡。",
            "/uploads/activities/lantern_festival.jpg",
            LocalDate.of(2026, 2, 12), LocalDate.of(2026, 2, 12), "完成灯会路线打卡", "60", "线下活动", 1));
        
        activityMapper.insert(createActivity("地球一小时活动",
            "响应全球环保行动，参与熄灯骑行活动。",
            "/uploads/activities/earth_hour.jpg",
            LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 21), "骑行并完成打卡", "40", "线上讲座", 0));
        
        log.info("活动数据已初始化（7条）");
    }
    
    private Activity createActivity(String title, String desc, String imageUrl, 
            LocalDate startDate, LocalDate endDate, String requirement, String points, String type, int status) {
        Activity a = new Activity();
        a.setTitle(title);
        a.setDescription(desc);
        a.setImageUrl(imageUrl);
        a.setStartDate(startDate);
        a.setEndDate(endDate);
        a.setRequirement(requirement);
        a.setRewardPoints(new BigDecimal(points));
        a.setType(type);
        a.setStatus(status);
        return a;
    }

    private void initAnnouncements() {
        if (announcementMapper.selectCount(null) > 0) return;

        Announcement an1 = new Announcement();
        an1.setTitle("平台正式上线公告");
        an1.setContent("亲爱的用户，低碳出行激励平台正式上线啦！平台致力于鼓励大家选择绿色出行方式，每次出行记录都能获得碳积分奖励。积分可以在商城兑换精美礼品，快来参与吧！");
        an1.setStatus(1);
        announcementMapper.insert(an1);
        
        Announcement an2 = new Announcement();
        an2.setTitle("积分兑换规则说明");
        an2.setContent("商城积分兑换已全面开放！积分永久有效，放心积累。目前支持兑换骑行装备、自行车配件等多种商品，更多好物持续上架中。");
        an2.setStatus(1);
        announcementMapper.insert(an2);
        
        Announcement an3 = new Announcement();
        an3.setTitle("绿色出行月活动开启");
        an3.setContent("三月绿色出行月活动正式开始！本月累计骑行50km即可获得100积分和「低碳达人」称号。快来参与挑战，赢取丰厚奖励！");
        an3.setStatus(1);
        announcementMapper.insert(an3);
        
        Announcement an4 = new Announcement();
        an4.setTitle("周末骑行活动预告");
        an4.setContent("本周六将组织城市骑行活动，路线全程约30km，沿途设有补给点。报名请在活动页面提交申请，完成全程可获得80积分。");
        an4.setStatus(1);
        announcementMapper.insert(an4);
        
        Announcement an5 = new Announcement();
        an5.setTitle("系统维护通知");
        an5.setContent("平台将于本周日凌晨2:00-6:00进行系统维护，届时部分功能将暂停使用。给您带来的不便敬请谅解。");
        an5.setStatus(1);
        announcementMapper.insert(an5);
        
        log.info("公告数据已初始化（5条）");
    }

    private void initForumSections() {
        if (forumSectionMapper.selectCount(null) > 0) return;

        ForumSection s1 = new ForumSection();
        s1.setName("经验分享");
        s1.setDescription("分享你的低碳出行故事和心得");
        s1.setSortOrder(1);
        s1.setStatus(1);
        forumSectionMapper.insert(s1);
        
        ForumSection s2 = new ForumSection();
        s2.setName("路线攻略");
        s2.setDescription("推荐优质出行路线，发现城市之美");
        s2.setSortOrder(2);
        s2.setStatus(1);
        forumSectionMapper.insert(s2);
        
        ForumSection s3 = new ForumSection();
        s3.setName("低碳问答");
        s3.setDescription("讨论低碳出行相关问题，互相学习");
        s3.setSortOrder(3);
        s3.setStatus(1);
        forumSectionMapper.insert(s3);
        
        log.info("论坛板块数据已初始化");
    }

    private void initForumPosts() {
        if (forumPostMapper.selectCount(null) > 0) return;
        
        User user1 = getUserByPhone("13900000001");
        User user2 = getUserByPhone("13900000002");
        User user3 = getUserByPhone("13900000003");
        if (user1 == null || user2 == null || user3 == null) return;

        ForumPost p1 = createPost(user1.getId(), 1L, "我的骑行通勤日记：从开车族到骑行达人",
            "分享我的骑行通勤经历。坚持骑行半年，从最初的几公里到现在每天20公里，不仅身体更健康了，每个月的交通费也省了不少。最重要的是，为环保贡献了自己的一份力量！\n\n骑行装备推荐：\n1. 头盔是必选项，安全第一\n2. 骑行手套可以有效减震\n3. 车灯夜间必备\n4. 背包要选透气的",
            156, 23, 1, 1);
        forumPostMapper.insert(p1);

        forumPostMapper.insert(createPost(user2.getId(), 1L, "公共交通也能很舒适 - 我的公交通勤经验",
            "很多人觉得公交拥挤不舒服，其实选对线路和时间很重要。\n\n1. 错峰出行，避开早高峰\n2. 选靠近前门的座位，下车方便\n3. 准备一本书或播客，时间过得很快\n4. 公交+步行组合，健康又环保\n\n一个月下来，我发现不仅节省了油钱，还多了很多碎片时间可以用来学习。",
            89, 15, 0, 1));

        forumPostMapper.insert(createPost(user3.getId(), 1L, "周末骑行的快乐，你根本想象不到！",
            "最近迷上了周末长距离骑行，发现了城市里很多平时开车注意不到的美景。\n\n推荐一条我常骑的路线：从滨江公园出发，沿着江边骑行到湿地公园，全程约25公里，风景超好！\n\n骑行的好处：\n- 锻炼身体\n- 放松心情\n- 低碳环保\n- 省钱",
            234, 42, 0, 1));

        forumPostMapper.insert(createPost(user1.getId(), 2L, "城市骑行路线推荐：适合新手的5条安全路线",
            "整理了5条适合新手的安全骑行路线，都是我亲自骑过验证过的：\n\n1. 滨江休闲线（约8公里）- 特点：路况好，人少景美\n2. 公园环线（约12公里）- 特点：全程有自行车道\n3. 高校穿越线（约10公里）- 特点：经过多所高校，氛围好\n4. 河堤风光线（约15公里）- 特点：视野开阔，空气好\n5. 老城探秘线（约8公里）- 特点：可以发现很多隐藏的美食",
            312, 56, 0, 1));

        forumPostMapper.insert(createPost(user2.getId(), 2L, "地铁+步行：我的黄金通勤组合",
            "家离地铁站1.5公里，地铁站离公司800米，这段距离我选择步行。\n\n实测数据：\n- 每天步行约5000步\n- 每周减少碳排放约5kg\n- 每月节省交通费约200元\n\n步行上班不仅锻炼身体，还能让我保持清醒的工作状态。强烈推荐！",
            145, 28, 0, 1));

        forumPostMapper.insert(createPost(user3.getId(), 3L, "骑行100公里能减少多少碳排放？算给你看！",
            "很多人问我骑行到底环保在哪里，今天来算一笔账：\n\n骑行100公里：\n- 碳排放：0 kg\n- 消耗热量：约2000卡路里\n\n开车100公里：\n- 碳排放：约20 kg CO2\n\n骑行不仅零排放，还锻炼身体，一举两得！",
            456, 78, 0, 1));

        forumPostMapper.insert(createPost(user1.getId(), 3L, "下雨天怎么绿色出行？这些方法学起来",
            "最近雨季来了，很多人问下雨天怎么坚持绿色出行：\n\n1. 地铁+步行 - 最稳定的选择\n2. 公交+共享单车 - 组合出行\n3. 电动滑板车 - 轻便快捷\n4. 实在不行，在家办公一天也是环保的选择哦！",
            198, 34, 0, 1));

        // 初始化评论
        ForumPost post1 = forumPostMapper.selectOne(new LambdaQueryWrapper<ForumPost>().likeRight(ForumPost::getTitle, "我的骑行通勤日记"));
        if (post1 != null) {
            forumCommentMapper.insert(createComment(post1.getId(), user2.getId(), "写得真好！我也想开始骑行通勤了，请问有什么入门建议吗？", 1, null, null));
            forumCommentMapper.insert(createComment(post1.getId(), user1.getId(), "回复楼上：建议先从短距离开始，5公里以内，等身体适应了再逐步增加距离。", 1, null, null));
            forumCommentMapper.insert(createComment(post1.getId(), user3.getId(), "支持！我每天骑行15公里上班，已经坚持两年了，身体状态比以前好很多。", 1, 2, "垃圾广告"));
        }

        ForumPost post2 = forumPostMapper.selectOne(new LambdaQueryWrapper<ForumPost>().likeRight(ForumPost::getTitle, "城市骑行路线推荐"));
        if (post2 != null) {
            forumCommentMapper.insert(createComment(post2.getId(), user3.getId(), "太实用了！周末就去试试那条老城探秘线", 1, null, null));
            forumCommentMapper.insert(createComment(post2.getId(), user2.getId(), "建议楼主再出一期进阶路线推荐！", 1, null, null));
        }
        
        log.info("论坛帖子和评论数据已初始化");
    }
    
    private ForumPost createPost(Long userId, Long sectionId, String title, String content, 
            int likes, int commentsCount, int isTop, int status) {
        ForumPost p = new ForumPost();
        p.setUserId(userId);
        p.setSectionId(sectionId);
        p.setTitle(title);
        p.setContent(content);
        p.setLikes(likes);
        p.setCommentsCount(commentsCount);
        p.setIsTop(isTop);
        p.setStatus(status);
        p.setCreatedAt(LocalDateTime.now().minusDays((int)(Math.random() * 15)));
        return p;
    }
    
    private ForumComment createComment(Long postId, Long userId, String content, int status, Integer reportCount, String reportReason) {
        ForumComment c = new ForumComment();
        c.setPostId(postId);
        c.setUserId(userId);
        c.setContent(content);
        c.setStatus(status);
        c.setReportCount(reportCount);
        c.setReportReason(reportReason);
        c.setCreatedAt(LocalDateTime.now().minusDays((int)(Math.random() * 10)));
        return c;
    }

    private void initTravelRecords() {
        if (travelRecordMapper.selectCount(null) > 0) return;

        User user1 = getUserByPhone("13900000001");
        User user2 = getUserByPhone("13900000002");
        User user3 = getUserByPhone("13900000003");
        if (user1 == null || user2 == null || user3 == null) return;

        travelRecordMapper.insert(createTravelRecord(user1.getId(), 2L, "公司", "家", "15.5", "3.26", "155", 1, 1));
        travelRecordMapper.insert(createTravelRecord(user1.getId(), 2L, "家", "公司", "20.0", "4.20", "200", 1, 3));
        travelRecordMapper.insert(createTravelRecord(user1.getId(), 3L, "小区门口", "购物中心", "8.5", "0.68", "68", 1, 5));
        travelRecordMapper.insert(createTravelRecord(user2.getId(), 1L, "家", "地铁站", "5.0", "0", "25", 1, 1));
        travelRecordMapper.insert(createTravelRecord(user2.getId(), 4L, "地铁A站", "地铁B站", "12.0", "0.48", "84", 1, 2));
        travelRecordMapper.insert(createTravelRecord(user2.getId(), 2L, "公园", "单位", "18.0", "3.78", "180", 1, 4));
        travelRecordMapper.insert(createTravelRecord(user2.getId(), 2L, "家", "郊外", "25.0", "5.25", "250", 0, 0));
        travelRecordMapper.insert(createTravelRecord(user3.getId(), 2L, "出发点", "终点", "30.0", "6.30", "300", 1, 1));
        travelRecordMapper.insert(createTravelRecord(user3.getId(), 3L, "社区", "医院", "10.0", "0.80", "80", 1, 6));
        travelRecordMapper.insert(createTravelRecord(user3.getId(), 5L, "学校", "商场", "12.0", "0.60", "72", 1, 8));

        log.info("出行记录数据已初始化（10条）");
    }

    private TravelRecord createTravelRecord(Long userId, Long travelModeId, String startLocation, String endLocation,
            String distance, String carbonReduction, String pointsEarned, int status, int daysAgo) {
        TravelRecord r = new TravelRecord();
        r.setUserId(userId);
        r.setTravelModeId(travelModeId);
        r.setStartLocation(startLocation);
        r.setEndLocation(endLocation);
        r.setDistance(new BigDecimal(distance));
        r.setCarbonReduction(new BigDecimal(carbonReduction));
        r.setPointsEarned(new BigDecimal(pointsEarned));
        r.setStatus(status);
        r.setTravelDate(LocalDate.now().minusDays(daysAgo));
        r.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        return r;
    }

    private void initActivityParticipations() {
        if (activityParticipationMapper.selectCount(null) > 0) return;
        
        User user1 = getUserByPhone("13900000001");
        User user2 = getUserByPhone("13900000002");
        User user3 = getUserByPhone("13900000003");
        if (user1 == null || user2 == null || user3 == null) return;

        Activity activity1 = activityMapper.selectOne(new LambdaQueryWrapper<Activity>().eq(Activity::getTitle, "绿色出行月"));
        Activity activity2 = activityMapper.selectOne(new LambdaQueryWrapper<Activity>().eq(Activity::getTitle, "无车日挑战"));
        Activity activity3 = activityMapper.selectOne(new LambdaQueryWrapper<Activity>().eq(Activity::getTitle, "低碳知识竞赛"));
        
        if (activity1 != null) {
            activityParticipationMapper.insert(createParticipation(user1.getId(), activity1.getId(), 
                "/uploads/activities/helmet.jpg", 1, "骑行数据核实无误，奖励已发放", 5));
            activityParticipationMapper.insert(createParticipation(user2.getId(), activity1.getId(), 
                "/uploads/activities/cycling_event.jpg", 0, null, 0));
        }
        
        if (activity2 != null) {
            activityParticipationMapper.insert(createParticipation(user3.getId(), activity2.getId(), 
                "/uploads/activities/raincoat.jpg", 1, "完成审核", 3));
            activityParticipationMapper.insert(createParticipation(user1.getId(), activity2.getId(), 
                "/uploads/activities/raincoat.jpg", 0, null, 0));
        }
        
        if (activity3 != null) {
            activityParticipationMapper.insert(createParticipation(user2.getId(), activity3.getId(), 
                "/uploads/activities/knowledge_quiz.jpg", 1, "答题正确率100%", 7));
        }
        
        log.info("活动参与数据已初始化");
    }
    
    private ActivityParticipation createParticipation(Long userId, Long activityId, String evidenceUrl, int status, String reviewComment, int daysAgo) {
        ActivityParticipation p = new ActivityParticipation();
        p.setUserId(userId);
        p.setActivityId(activityId);
        p.setEvidenceUrl(evidenceUrl);
        p.setStatus(status);
        p.setReviewComment(reviewComment);
        p.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        return p;
    }

    private void initOrders() {
        if (orderMapper.selectCount(null) > 0) return;
        
        User user1 = getUserByPhone("13900000001");
        User user2 = getUserByPhone("13900000002");
        if (user1 == null || user2 == null) return;

        Product product1 = productMapper.selectOne(new LambdaQueryWrapper<Product>().eq(Product::getName, "智能骑行头盔"));
        Product product2 = productMapper.selectOne(new LambdaQueryWrapper<Product>().eq(Product::getName, "便携雨衣"));
        Product product3 = productMapper.selectOne(new LambdaQueryWrapper<Product>().eq(Product::getName, "骑行手套"));

        if (product1 != null) {
            Order o1 = new Order();
            o1.setOrderNo("ORD" + System.currentTimeMillis() + "001");
            o1.setUserId(user1.getId());
            o1.setProductId(product1.getId());
            o1.setQuantity(1);
            o1.setPointsSpent(new BigDecimal("200"));
            o1.setStatus(2);
            o1.setDeliveryAddress("北京市朝阳区建国路88号");
            o1.setDeliveryNo("SF1234567890");
            o1.setCreatedAt(LocalDateTime.now().minusDays(10));
            orderMapper.insert(o1);
        }

        if (product2 != null) {
            Order o2 = new Order();
            o2.setOrderNo("ORD" + (System.currentTimeMillis() + 1) + "002");
            o2.setUserId(user1.getId());
            o2.setProductId(product2.getId());
            o2.setQuantity(1);
            o2.setPointsSpent(new BigDecimal("80"));
            o2.setStatus(1);
            o2.setDeliveryAddress("北京市朝阳区建国路88号");
            o2.setDeliveryNo("YT9876543210");
            o2.setCreatedAt(LocalDateTime.now().minusDays(2));
            orderMapper.insert(o2);
        }

        if (product3 != null) {
            Order o3 = new Order();
            o3.setOrderNo("ORD" + (System.currentTimeMillis() + 2) + "003");
            o3.setUserId(user2.getId());
            o3.setProductId(product3.getId());
            o3.setQuantity(2);
            o3.setPointsSpent(new BigDecimal("160"));
            o3.setStatus(0);
            o3.setDeliveryAddress("上海市浦东新区世纪大道100号");
            o3.setCreatedAt(LocalDateTime.now().minusHours(3));
            orderMapper.insert(o3);
        }
        
        log.info("订单数据已初始化（3条）");
    }
    
    private User getUserByPhone(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    private void initPointsRules() {
        if (pointsRuleMapper.selectCount(null) > 0) return;

        List<TravelMode> modes = travelModeMapper.selectList(null);
        for (TravelMode mode : modes) {
            PointsRule rule = new PointsRule();
            rule.setTravelModeId(mode.getId());
            rule.setPointsPerKm(mode.getPointsPerKm());
            rule.setCarbonReduction(mode.getCarbonReduction());
            rule.setStatus(1);
            rule.setDescription(mode.getName() + "积分规则");
            pointsRuleMapper.insert(rule);
        }
        log.info("积分规则数据已初始化（{}条）", modes.size());
    }

    private void initPointsDetails() {
        if (pointsDetailMapper.selectCount(null) > 0) return;

        List<TravelRecord> records = travelRecordMapper.selectList(
            new LambdaQueryWrapper<TravelRecord>().eq(TravelRecord::getStatus, 1));
        for (TravelRecord record : records) {
            PointsDetail detail = new PointsDetail();
            detail.setUserId(record.getUserId());
            detail.setPoints(record.getPointsEarned());
            detail.setType("travel");
            detail.setSourceId(record.getId());
            detail.setDescription("出行获得积分");
            detail.setCreatedAt(record.getCreatedAt());
            pointsDetailMapper.insert(detail);
        }

        // 已通过的活动参与奖励
        List<ActivityParticipation> participations = activityParticipationMapper.selectList(
            new LambdaQueryWrapper<ActivityParticipation>().eq(ActivityParticipation::getStatus, 1));
        for (ActivityParticipation p : participations) {
            Activity activity = activityMapper.selectById(p.getActivityId());
            if (activity == null) continue;
            PointsDetail detail = new PointsDetail();
            detail.setUserId(p.getUserId());
            detail.setPoints(activity.getRewardPoints());
            detail.setType("activity");
            detail.setSourceId(p.getActivityId());
            detail.setDescription("参与活动「" + activity.getTitle() + "」获得积分");
            detail.setCreatedAt(p.getCreatedAt());
            pointsDetailMapper.insert(detail);
        }

        // 已兑换的订单消耗
        List<Order> orders = orderMapper.selectList(
            new LambdaQueryWrapper<Order>().ne(Order::getStatus, 3));
        for (Order order : orders) {
            Product product = productMapper.selectById(order.getProductId());
            PointsDetail detail = new PointsDetail();
            detail.setUserId(order.getUserId());
            detail.setPoints(order.getPointsSpent().negate());
            detail.setType("exchange");
            detail.setSourceId(order.getId());
            detail.setDescription("兑换商品「" + (product != null ? product.getName() : "商品") + "」消耗积分");
            detail.setCreatedAt(order.getCreatedAt());
            pointsDetailMapper.insert(detail);
        }

        log.info("积分明细数据已初始化");

        // 根据明细汇总回写 carbon_points
        List<PointsDetail> allDetails = pointsDetailMapper.selectList(null);
        Map<Long, BigDecimal[]> summary = new java.util.HashMap<>();
        for (PointsDetail d : allDetails) {
            summary.computeIfAbsent(d.getUserId(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (d.getPoints().compareTo(BigDecimal.ZERO) > 0) {
                summary.get(d.getUserId())[0] = summary.get(d.getUserId())[0].add(d.getPoints());
            } else {
                summary.get(d.getUserId())[1] = summary.get(d.getUserId())[1].add(d.getPoints().abs());
            }
        }
        // 汇总出行减碳量
        Map<Long, BigDecimal> carbonMap = new java.util.HashMap<>();
        List<TravelRecord> approvedRecords = travelRecordMapper.selectList(
            new LambdaQueryWrapper<TravelRecord>().eq(TravelRecord::getStatus, 1));
        for (TravelRecord r : approvedRecords) {
            carbonMap.merge(r.getUserId(), r.getCarbonReduction(), BigDecimal::add);
        }

        summary.forEach((userId, pts) -> {
            CarbonPoints cp = carbonPointsMapper.selectOne(
                new LambdaQueryWrapper<CarbonPoints>().eq(CarbonPoints::getUserId, userId));
            if (cp != null) {
                cp.setTotalPoints(pts[0]);
                cp.setUsedPoints(pts[1]);
                cp.setAvailablePoints(pts[0].subtract(pts[1]));
                cp.setTotalCarbon(carbonMap.getOrDefault(userId, BigDecimal.ZERO));
                carbonPointsMapper.updateById(cp);
            }
        });
        log.info("碳积分账户已根据明细同步更新");
    }
}
