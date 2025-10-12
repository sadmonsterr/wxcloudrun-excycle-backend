package com.excycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.excycle.common.Result;
import com.excycle.context.UserContext;
import com.excycle.entity.Address;
import com.excycle.entity.User;
import com.excycle.entity.UserWallet;
import com.excycle.mapper.AddressMapper;
import com.excycle.mapper.UserMapper;
import com.excycle.service.FinanceService;
import com.excycle.service.RoleService;
import com.excycle.service.UserService;
import com.excycle.dto.UserRegisterRequest;
import com.excycle.service.UserShopRoleService;
import com.excycle.vo.AddressVO;
import com.excycle.vo.RoleVO;
import com.excycle.vo.ShopRoleVO;
import com.excycle.vo.UserShopRoleVO;
import com.excycle.vo.UserVO;
import com.excycle.utils.UUIDUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    private final AddressMapper addressMapper;

    private final FinanceService financeService;

    private final RoleService roleService;

    private final UserShopRoleService userShopRoleService;

    public UserServiceImpl(UserMapper userMapper, AddressMapper addressMapper, FinanceService financeService, RoleService roleService, UserShopRoleService userShopRoleService) {
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
        this.financeService = financeService;
        this.roleService = roleService;
        this.userShopRoleService = userShopRoleService;
    }

    @Override
    public Page<UserVO> getUserPage(Page<User> page, User user) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            queryWrapper.like(User::getPhone, user.getPhone());
        }

        if (user.getName() != null && !user.getName().isEmpty()) {
            queryWrapper.like(User::getName, user.getName());
        }

        queryWrapper.orderByDesc(User::getCreatedAt);

        Page<User> userPage = baseMapper.selectPage(page, queryWrapper);
        // 转换为VO
        Page<UserVO> userVOPage = new Page<>();
        userVOPage.setCurrent(userPage.getCurrent());
        userVOPage.setSize(userPage.getSize());
        userVOPage.setTotal(userPage.getTotal());
        userVOPage.setRecords(userPage.getRecords().stream().map(this::convertToUserVO).collect(Collectors.toList()));
        return userVOPage;
    }

    @Override
    public Page<UserVO> getDriverUserPage(Page<User> page, String searchWord) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getRole, "driver")
                .nested(StringUtils.isNotBlank(searchWord), nested -> nested.like(User::getName, searchWord).or()
                        .like(User::getPhone, searchWord));
        queryWrapper.orderByDesc(User::getCreatedAt);
        Page<User> driverUserPage = baseMapper.selectPage(page, queryWrapper);
        Page<UserVO> userVOPage = new Page<>();
        userVOPage.setCurrent(driverUserPage.getCurrent());
        userVOPage.setSize(driverUserPage.getSize());
        userVOPage.setTotal(driverUserPage.getTotal());
        userVOPage.setRecords(driverUserPage.getRecords().stream().map(this::convertToSimpleDriverUserVO).collect(Collectors.toList()));
        return userVOPage;
    }

    private UserVO convertToSimpleDriverUserVO(User user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setName(String.format("%s(%s)", user.getName(), user.getPhone()));
        return userVO;
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
    public boolean deleteUser(String id) {
        return removeById(id);
    }

    @Override
    public UserVO getById(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        UserVO userVO = convertToUserVO(user);
        userVO.setShopRoles(getUserShopRoles(user.getId()));
        return userVO;
    }

    @Override
    public UserVO getByOpenId(String openId) {
        User user = userMapper.selectByOpenId(openId);
        if (user == null) {
            return null;
        }
        UserVO userVO = convertToUserVO(user);
        userVO.setShopRoles(getUserShopRoles(user.getId()));
        return userVO;
    }

    private List<ShopRoleVO> getUserShopRoles(String userId) {
        List<UserShopRoleVO> shopRoles = userShopRoleService.getUserShopRolesByUserId(userId);
        return shopRoles.stream().map(shopRole -> {
            ShopRoleVO shopRoleVO = new ShopRoleVO();
            BeanUtils.copyProperties(shopRole, shopRoleVO);
            return shopRoleVO;
        }).collect(Collectors.toList());
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
        Optional.ofNullable(roleService.getRoleById(user.getRole()))
                .ifPresent(roleVO -> userVO.setRoleName(roleVO.getName()));
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

    @Override
    public UserWallet getUserWallet(String openId) {
        return financeService.getUserWallet(openId);
    }
}