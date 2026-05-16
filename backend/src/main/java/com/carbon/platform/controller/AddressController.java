package com.carbon.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carbon.platform.dto.ApiResponse;
import com.carbon.platform.entity.Address;
import com.carbon.platform.mapper.AddressMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressMapper addressMapper;

    @GetMapping
    public ApiResponse<?> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Address> addresses = addressMapper.selectList(
            new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreatedAt)
        );
        return ApiResponse.success(addresses);
    }

    @PostMapping
    public ApiResponse<?> add(@RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0));
        }
        
        address.setUserId(userId);
        address.setFullAddress(
            (address.getProvince() != null ? address.getProvince() : "") +
            (address.getCity() != null ? address.getCity() : "") +
            (address.getDistrict() != null ? address.getDistrict() : "") +
            (address.getDetailAddress() != null ? address.getDetailAddress() : "")
        );
        addressMapper.insert(address);
        return ApiResponse.success(address);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Address existing = addressMapper.selectOne(
            new LambdaQueryWrapper<Address>().eq(Address::getId, id).eq(Address::getUserId, userId)
        );
        if (existing == null) {
            return ApiResponse.error("地址不存在");
        }

        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .ne(Address::getId, id)
                .set(Address::getIsDefault, 0));
        }

        address.setId(id);
        address.setUserId(userId);
        address.setFullAddress(
            (address.getProvince() != null ? address.getProvince() : "") +
            (address.getCity() != null ? address.getCity() : "") +
            (address.getDistrict() != null ? address.getDistrict() : "") +
            (address.getDetailAddress() != null ? address.getDetailAddress() : "")
        );
        addressMapper.updateById(address);
        return ApiResponse.success(address);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Address existing = addressMapper.selectOne(
            new LambdaQueryWrapper<Address>().eq(Address::getId, id).eq(Address::getUserId, userId)
        );
        if (existing == null) {
            return ApiResponse.error("地址不存在");
        }
        addressMapper.deleteById(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/default")
    public ApiResponse<?> setDefault(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        addressMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Address>()
            .eq(Address::getUserId, userId)
            .set(Address::getIsDefault, 0));
        Address address = addressMapper.selectById(id);
        if (address != null && address.getUserId().equals(userId)) {
            address.setIsDefault(1);
            addressMapper.updateById(address);
        }
        return ApiResponse.success(null);
    }

    @GetMapping("/default")
    public ApiResponse<?> getDefault(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Address address = addressMapper.selectOne(
            new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
        );
        if (address == null) {
            address = addressMapper.selectOne(
                new LambdaQueryWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .orderByDesc(Address::getCreatedAt)
                    .last("LIMIT 1")
            );
        }
        return ApiResponse.success(address);
    }
}