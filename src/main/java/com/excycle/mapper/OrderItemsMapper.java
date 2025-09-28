package com.excycle.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.excycle.entity.OrderItems;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderItemsMapper extends BaseMapper<OrderItems> {

    default List<OrderItems> getListByOrderId(String orderId) {
        LambdaQueryWrapper<OrderItems> queryWrapper = new LambdaQueryWrapper<OrderItems>()
                .eq(OrderItems::getOrderId, orderId);
        return selectList(queryWrapper);
    };

}
