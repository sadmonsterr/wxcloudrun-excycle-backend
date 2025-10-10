package com.excycle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.excycle.dto.UserShopRoleDTO;
import com.excycle.entity.UserShopRole;
import com.excycle.vo.ShopRoleVO;
import com.excycle.vo.UserShopRoleVO;

import java.util.List;

public interface UserShopRoleService extends IService<UserShopRole> {
    Page<UserShopRoleVO> getUserShopRolePage(int page, int size, String userId, String shopId, String roleId);

    UserShopRoleVO getUserShopRoleById(String id);

    ShopRoleVO getShopRolesByUserId(String userId);

    List<UserShopRoleVO> getUserShopRolesByUserId(String userId);

    List<UserShopRoleVO> getUserShopRolesByShopId(String shopId);

    UserShopRoleVO createUserShopRole(UserShopRoleDTO userShopRoleDTO);

    UserShopRoleVO updateUserShopRole(String id, UserShopRoleDTO userShopRoleDTO);

    boolean deleteUserShopRole(String id);

    boolean deleteUserShopRoleByUserAndShop(String userId, String shopId);
}