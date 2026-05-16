package com.carbon.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carbon.platform.entity.Address;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}