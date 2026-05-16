package com.carbon.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbon.platform.entity.*;
import com.carbon.platform.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final CarbonPointsMapper carbonPointsMapper;
    private final PointsDetailMapper pointsDetailMapper;
    private final com.carbon.platform.mapper.MessageMapper messageMapper;
    private final UserMapper userMapper;

    public Page<Product> getProducts(String category, int page, int size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
            .eq(Product::getStatus, 1)
            .orderByAsc(Product::getSortOrder);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Product::getCategory, category);
        }
        return productMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public Product getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return product;
    }

    @Transactional
    public Order exchangeProduct(Long userId, Long productId, Integer quantity, String address) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) throw new RuntimeException("商品不存在");
        if (product.getStock() < quantity) throw new RuntimeException("库存不足");

        BigDecimal totalPoints = product.getPointsRequired().multiply(BigDecimal.valueOf(quantity));

        CarbonPoints cp = carbonPointsMapper.selectOne(
            new LambdaQueryWrapper<CarbonPoints>().eq(CarbonPoints::getUserId, userId)
        );
        if (cp.getAvailablePoints().compareTo(totalPoints) < 0) {
            throw new RuntimeException("积分不足");
        }

        cp.setAvailablePoints(cp.getAvailablePoints().subtract(totalPoints));
        cp.setUsedPoints(cp.getUsedPoints().add(totalPoints));
        carbonPointsMapper.updateById(cp);

        product.setStock(product.getStock() - quantity);
        productMapper.updateById(product);

        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setPointsSpent(totalPoints);
        order.setStatus(0);
        order.setDeliveryAddress(address);
        orderMapper.insert(order);

        PointsDetail detail = new PointsDetail();
        detail.setUserId(userId);
        detail.setPoints(totalPoints.negate());
        detail.setType("exchange");
        detail.setSourceId(order.getId());
        detail.setDescription("兑换商品：" + product.getName());
        pointsDetailMapper.insert(detail);

        // 发送兑换成功消息给用户
        Message msg = new Message();
        msg.setSenderId(userId);
        msg.setReceiverId(userId);
        msg.setTitle("兑换成功");
        msg.setContent("您已成功兑换 " + product.getName() + "，订单号：" + order.getOrderNo());
        msg.setType("exchange");
        msg.setIsRead(0);
        messageMapper.insert(msg);

        // 通知管理员有新的订单待处理
        notifyAdmins("新的兑换订单", "用户兑换了「" + product.getName() + "」，消耗 " + totalPoints + " 积分");

        return order;
    }

    // 通知所有管理员
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

    public Page<Order> getUserOrders(Long userId, int page, int size) {
        return orderMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt)
        );
    }
}
