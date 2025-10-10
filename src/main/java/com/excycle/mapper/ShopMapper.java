package com.excycle.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.excycle.entity.Shop;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShopMapper extends BaseMapper<Shop> {

    default Shop getByShopId(String shopId) {
        return this.selectOne(new LambdaQueryWrapper<Shop>().eq(Shop::getShopId, shopId));
    }
}