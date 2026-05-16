package com.carbon.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carbon.platform.entity.TravelRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface TravelRecordMapper extends BaseMapper<TravelRecord> {
    
    @Select("SELECT COALESCE(SUM(distance), 0) FROM travel_records WHERE user_id = #{userId}")
    BigDecimal selectTotalDistance(Long userId);
    
    @Select("SELECT travel_mode_id, COUNT(*) as count FROM travel_records WHERE status = 1 GROUP BY travel_mode_id")
    List<Map<String, Object>> selectCountByMode();
    
    @Select("SELECT COUNT(*) FROM travel_records WHERE user_id = #{userId}")
    Long selectCountByUserId(Long userId);
}
