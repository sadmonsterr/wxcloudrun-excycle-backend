package com.excycle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.excycle.entity.User;
import com.excycle.dto.UserRegisterRequest;
import com.excycle.entity.UserWallet;
import com.excycle.vo.AddressVO;
import com.excycle.vo.UserVO;

import java.util.List;

public interface UserService extends IService<User> {

    Page<UserVO> getUserPage(Page<User> page, User user);

    Page<UserVO> getDriverUserPage(Page<User> page, String searchWord);

    boolean addUser(User user);

    boolean updateUser(User user);

    boolean deleteUser(String id);

    UserVO getById(String userId);

    UserVO getByOpenId(String openId);

    Page<AddressVO> queryAddresses(String openId);

    User registerWithAddress(UserRegisterRequest registerRequest);

    UserWallet getUserWallet(String openId);
}