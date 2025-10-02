package com.excycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.excycle.context.UserContext;
import com.excycle.entity.Address;
import com.excycle.entity.User;
import com.excycle.mapper.AddressMapper;
import com.excycle.mapper.UserMapper;
import com.excycle.service.UserService;
import com.excycle.dto.UserRegisterRequest;
import com.excycle.vo.AddressVO;
import com.excycle.vo.UserVO;
import com.excycle.utils.UUIDUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    private final AddressMapper addressMapper;

    public UserServiceImpl(UserMapper userMapper, AddressMapper addressMapper) {
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
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
        return user == null ? null :convertToUserVO(user);
    }

    @Override
    public Page<AddressVO> queryAddresses(String openId) {
        Page<Address> addressPage = addressMapper.queryListByOpenId(openId);

        List<AddressVO> addresses = addressPage.getRecords().stream().map(address -> {
            AddressVO addressVO = new AddressVO();
            BeanUtils.copyProperties(address, addressVO);
            return addressVO;
        }).collect(Collectors.toList());

        Page<AddressVO> addressVOPage = new Page<>();
        BeanUtils.copyProperties(addressPage, addresses);
        addressVOPage.setRecords(addresses);
        return addressVOPage;
    }

    private UserVO convertToUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User registerWithAddress(UserRegisterRequest registerRequest) {
        // 获取当前用户的 openId
        String openId = UserContext.getCurrentOpenId();
        if (openId == null) {
            throw new RuntimeException("用户身份验证失败");
        }

        User existingUser = userMapper.selectByOpenId(openId);
        if (existingUser != null) {
            throw new RuntimeException("用户已存在");
        }

        // 创建地址
        Address address = new Address();
        address.setId(UUIDUtils.nextBase62SnowflakeId());
        address.setName(registerRequest.getName());
        address.setPhone(registerRequest.getPhone());
        address.setProvince(registerRequest.getProvince());
        address.setCity(registerRequest.getCity());
        address.setDistrict(registerRequest.getDistrict());
        address.setDetail(registerRequest.getAddress());
        address.setIsDefault(true);
        address.setOpenId(openId);
        addressMapper.insert(address);

        // 创建用户
        User user = new User();
        user.setId(UUIDUtils.nextBase62SnowflakeId());
        user.setName(registerRequest.getName());
        user.setPhone(registerRequest.getPhone());
        user.setBusinessLicense(registerRequest.getBusinessLicense());
        user.setRole(registerRequest.getRole());
        user.setCompany(registerRequest.getCompany());
        user.setShop(registerRequest.getStoreName());
        user.setOpenId(openId);
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());

        save(user);

        return user;
    }
}