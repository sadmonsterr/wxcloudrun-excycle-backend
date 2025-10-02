package com.excycle.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.excycle.entity.Address;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {

    default Page<Address> queryListByOpenId(String openId) {
        Page<Address> page = new Page<>();
        page.setCurrent(1);
        page.setSize(20);
        return this.selectPage(page, new LambdaQueryWrapper<Address>()
                .eq(Address::getOpenId, openId));
    }

}
