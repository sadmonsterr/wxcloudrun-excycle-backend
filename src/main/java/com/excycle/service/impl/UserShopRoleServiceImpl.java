package com.excycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.excycle.dto.UserShopRoleDTO;
import com.excycle.entity.Shop;
import com.excycle.entity.User;
import com.excycle.entity.UserShopRole;
import com.excycle.entity.Role;
import com.excycle.mapper.UserShopRoleMapper;
import com.excycle.mapper.UserMapper;
import com.excycle.mapper.ShopMapper;
import com.excycle.mapper.RoleMapper;
import com.excycle.service.UserShopRoleService;
import com.excycle.utils.UUIDUtils;
import com.excycle.vo.ShopRoleVO;
import com.excycle.vo.UserShopRoleVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class UserShopRoleServiceImpl extends ServiceImpl<UserShopRoleMapper, UserShopRole> implements UserShopRoleService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private RoleMapper roleMapper;



    @Override
    public Page<UserShopRoleVO> getUserShopRolePage(int page, int size, String userId, String shopId, String roleId) {
        Page<UserShopRole> userShopRolePage = new Page<>(page, size);
        LambdaQueryWrapper<UserShopRole> queryWrapper = new LambdaQueryWrapper<UserShopRole>()
                .eq(UserShopRole::getDeleted, 0)
                .eq(StringUtils.isNotBlank(userId), UserShopRole::getUserId, userId)
                .eq(StringUtils.isNotBlank(shopId), UserShopRole::getShopId, shopId)
                .eq(StringUtils.isNotBlank(roleId), UserShopRole::getRoleId, roleId)
                .orderByDesc(UserShopRole::getId);

        Page<UserShopRole> resultPage = page(userShopRolePage, queryWrapper);

        Page<UserShopRoleVO> voPage = new Page<>();
        BeanUtils.copyProperties(resultPage, voPage);
        voPage.setRecords(convertToVOBatch(resultPage.getRecords()));

        return voPage;
    }



    @Override
    public UserShopRoleVO getUserShopRoleById(String id) {
        UserShopRole userShopRole = getById(id);
        return userShopRole != null ? convertToVO(userShopRole) : null;
    }

    @Override
    public ShopRoleVO getShopRolesByUserId(String userId) {
        return null;
    }

    @Override
    public List<UserShopRoleVO> getUserShopRolesByUserId(String userId) {
        LambdaQueryWrapper<UserShopRole> queryWrapper = new LambdaQueryWrapper<UserShopRole>()
                .select(UserShopRole::getId, UserShopRole::getShopId, UserShopRole::getRoleId)
                .eq(UserShopRole::getUserId, userId)
                .eq(UserShopRole::getDeleted, 0);
        return convertToVOBatch(list(queryWrapper));
    }

    @Override
    public List<UserShopRoleVO> getUserShopRolesByShopId(String shopId) {
        LambdaQueryWrapper<UserShopRole> queryWrapper = new LambdaQueryWrapper<UserShopRole>()
                .eq(UserShopRole::getShopId, shopId)
                .eq(UserShopRole::getDeleted, 0);
        return convertToVOBatch(list(queryWrapper));
    }

    @Override
    public UserShopRoleVO createUserShopRole(UserShopRoleDTO userShopRoleDTO) {
        LambdaQueryWrapper<UserShopRole> queryWrapper = new LambdaQueryWrapper<UserShopRole>()
                .eq(UserShopRole::getUserId, userShopRoleDTO.getUserId())
                .eq(UserShopRole::getDeleted, 0);
        if (count(queryWrapper) != 0) {
            throw new IllegalArgumentException("请不要给一个用户绑定多个角色");
        }
        UserShopRole userShopRole = new UserShopRole();
        BeanUtils.copyProperties(userShopRoleDTO, userShopRole);
        userShopRole.setDeleted(0L);
        save(userShopRole);
        return convertToVO(userShopRole);
    }

    @Override
    public UserShopRoleVO updateUserShopRole(String id, UserShopRoleDTO userShopRoleDTO) {
        UserShopRole userShopRole = getById(id);
        if (userShopRole == null) {
            return null;
        }
        BeanUtils.copyProperties(userShopRoleDTO, userShopRole);
        updateById(userShopRole);
        return convertToVO(userShopRole);
    }

    @Override
    public boolean deleteUserShopRole(String id) {
        return getBaseMapper().deleteById(id, System.currentTimeMillis());
    }

    @Override
    public boolean deleteUserShopRoleByUserAndShop(String userId, String shopId) {
        LambdaQueryWrapper<UserShopRole> queryWrapper = new LambdaQueryWrapper<UserShopRole>()
                .eq(UserShopRole::getUserId, userId)
                .eq(UserShopRole::getShopId, shopId)
                .eq(UserShopRole::getDeleted, 0);

        List<UserShopRole> userShopRoles = list(queryWrapper);
        if (!userShopRoles.isEmpty()) {
            return userShopRoles.stream()
                    .allMatch(userShopRole -> getBaseMapper().deletedByUserAndShop(userId, shopId, System.currentTimeMillis()));

        }
        return false;
    }

    private List<UserShopRoleVO> convertToVOBatch(List<UserShopRole> userShopRoles) {
        if (userShopRoles.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> userIds = userShopRoles.stream()
                .map(UserShopRole::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> shopIds = userShopRoles.stream()
                .map(UserShopRole::getShopId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> roleIds = userShopRoles.stream()
                .map(UserShopRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().in(User::getId, userIds)
            );
            userNameMap.putAll(users.stream()
                    .collect(Collectors.toMap(User::getId, User::getName)));
        }

        Map<String, String> shopNameMap = new HashMap<>();
        if (!shopIds.isEmpty()) {
            List<Shop> shops = shopMapper.selectList(
                new LambdaQueryWrapper<Shop>().in(Shop::getShopId, shopIds)
            );
            shopNameMap.putAll(shops.stream()
                    .collect(Collectors.toMap(Shop::getShopId, Shop::getName)));
        }

        Map<String, String> roleNameMap = new HashMap<>();
        if (!roleIds.isEmpty()) {
            List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>().in(Role::getRoleId, roleIds)
            );
            roleNameMap.putAll(roles.stream()
                    .collect(Collectors.toMap(Role::getRoleId, Role::getName)));
        }

        return userShopRoles.stream()
                .map(userShopRole -> {
                    UserShopRoleVO vo = new UserShopRoleVO();
                    BeanUtils.copyProperties(userShopRole, vo);
                    vo.setUserName(userNameMap.get(userShopRole.getUserId()));
                    vo.setShopName(shopNameMap.get(userShopRole.getShopId()));
                    vo.setRoleName(roleNameMap.get(userShopRole.getRoleId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private UserShopRoleVO convertToVO(UserShopRole userShopRole) {
        return convertToVOBatch(Collections.singletonList(userShopRole)).get(0);
    }
}