package com.excycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.excycle.entity.Item;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {
}