package com.excycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.excycle.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}