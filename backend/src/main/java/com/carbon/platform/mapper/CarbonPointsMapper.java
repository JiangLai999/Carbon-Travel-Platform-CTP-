package com.carbon.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carbon.platform.entity.CarbonPoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CarbonPointsMapper extends BaseMapper<CarbonPoints> {
    
    @Select("SELECT COUNT(*) + 1 FROM carbon_points WHERE total_points > (SELECT COALESCE(total_points, 0) FROM carbon_points WHERE user_id = #{userId})")
    Long selectRank(Long userId);
    
    @Select("SELECT COALESCE(SUM(total_points), 0) FROM carbon_points")
    java.math.BigDecimal selectTotalPoints();
    
    @Select("SELECT COALESCE(SUM(total_carbon), 0) FROM carbon_points")
    java.math.BigDecimal selectTotalCarbon();
}
