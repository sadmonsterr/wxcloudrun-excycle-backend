package com.excycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.excycle.entity.User;
import com.excycle.mapper.UserMapper;
import com.excycle.service.UserService;
import com.excycle.vo.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Page<User> getUserPage(Page<User> page, User user) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            queryWrapper.like(User::getPhone, user.getPhone());
        }

        if (user.getName() != null && !user.getName().isEmpty()) {
            queryWrapper.like(User::getName, user.getName());
        }

        queryWrapper.orderByDesc(User::getCreatedAt);

        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Page<User> getDriverUserPage(Page<User> page, String searchWord) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .nested(StringUtils.isNotBlank(searchWord), nested -> nested.like(User::getName, searchWord).or()
                        .like(User::getPhone, searchWord));
        queryWrapper.orderByDesc(User::getCreatedAt);
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public boolean addUser(User user) {
        return save(user);
    }

    @Override
    public boolean updateUser(User user) {
        return updateById(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        return removeById(id);
    }

    @Override
    public UserVO getByOpenId(String openId) {
        User user = userMapper.selectByOpenId(openId);
        return convertToUserVO(user);
    }

    private UserVO convertToUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}