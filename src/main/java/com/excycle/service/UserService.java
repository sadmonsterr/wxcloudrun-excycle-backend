package com.excycle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.excycle.entity.User;
import com.excycle.vo.UserVO;

public interface UserService extends IService<User> {

    Page<User> getUserPage(Page<User> page, User user);

    Page<User> getDriverUserPage(Page<User> page, String searchWord);

    boolean addUser(User user);

    boolean updateUser(User user);

    boolean deleteUser(Long id);

    UserVO getByOpenId(String openId);
}