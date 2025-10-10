package com.excycle.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.excycle.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    default Map<String, String> queryUsernameByOpenIds(List<String> openIds) {
        Map<String, String> usersByOpenId = new HashMap<>();
        if (!openIds.isEmpty()) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(User::getOpenId, User::getName);
            queryWrapper.in(User::getOpenId, openIds);
            List<User> users = selectList(queryWrapper);
            usersByOpenId.putAll(users.stream()
                    .collect(Collectors.toMap(User::getOpenId, User::getName)));
        }
        return usersByOpenId;
    }


    default User selectByOpenId(String openId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getOpenId, openId);
        return selectOne(queryWrapper);
    }

}