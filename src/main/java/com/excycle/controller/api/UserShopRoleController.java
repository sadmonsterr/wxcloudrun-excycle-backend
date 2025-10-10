package com.excycle.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.excycle.common.Result;
import com.excycle.dto.UserShopRoleDTO;
import com.excycle.service.UserShopRoleService;
import com.excycle.vo.UserShopRoleVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/user-shop-roles")
public class UserShopRoleController {

    @Autowired
    private UserShopRoleService userShopRoleService;

    @Data
    public static class UserShopRoleQueryRequest {
        private int page = 1;
        private int size = 10;
        private String userId;
        private String shopId;
        private String roleId;
    }

    /**
     * 获取用户店铺角色列表
     * POST /api/v1/user-shop-roles/list
     */
    @PostMapping("/list")
    public Result<Page<UserShopRoleVO>> getUserShopRoles(@RequestBody UserShopRoleQueryRequest queryRequest) {
        return Result.success(userShopRoleService.getUserShopRolePage(queryRequest.getPage(), queryRequest.getSize(), queryRequest.getUserId(), queryRequest.getShopId(), queryRequest.getRoleId()));
    }

    /**
     * 根据ID获取用户店铺角色
     * GET /api/v1/user-shop-roles/{id}
     */
    @GetMapping("/{id}")
    public Result<UserShopRoleVO> getUserShopRole(@PathVariable String id) {
        return Result.success(userShopRoleService.getUserShopRoleById(id));
    }

    /**
     * 根据用户ID获取用户店铺角色
     * GET /api/v1/user-shop-roles/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public Result<List<UserShopRoleVO>> getUserShopRolesByUserId(@PathVariable String userId) {
        return Result.success(userShopRoleService.getUserShopRolesByUserId(userId));
    }

    /**
     * 根据店铺ID获取用户店铺角色
     * GET /api/v1/user-shop-roles/shop/{shopId}
     */
    @GetMapping("/shop/{shopId}")
    public Result<List<UserShopRoleVO>> getUserShopRolesByShopId(@PathVariable String shopId) {
        return Result.success(userShopRoleService.getUserShopRolesByShopId(shopId));
    }

    /**
     * 创建用户店铺角色
     * POST /api/v1/user-shop-roles
     */
    @PostMapping
    public Result<UserShopRoleVO> createUserShopRole(@Validated @RequestBody UserShopRoleDTO userShopRoleDTO) {
        return Result.success("用户店铺角色创建成功", userShopRoleService.createUserShopRole(userShopRoleDTO));
    }

    /**
     * 更新用户店铺角色
     * POST /api/v1/user-shop-roles/{id}
     */
    @PostMapping("/{id}")
    public Result<UserShopRoleVO> updateUserShopRole(@PathVariable String id, @Validated @RequestBody UserShopRoleDTO userShopRoleDTO) {
        return Result.success("用户店铺角色更新成功", userShopRoleService.updateUserShopRole(id, userShopRoleDTO));
    }

    /**
     * 删除用户店铺角色
     * DELETE /api/v1/user-shop-roles/{id}
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUserShopRole(@PathVariable String id) {
        userShopRoleService.deleteUserShopRole(id);
        return Result.success("用户店铺角色删除成功");
    }

    /**
     * 批量删除用户店铺角色
     * DELETE /api/v1/user-shop-roles
     */
    @DeleteMapping
    public Result<String> batchDeleteUserShopRoles(@RequestParam List<String> ids) {
        // TODO: 实现批量删除逻辑
        return Result.success("批量删除成功");
    }

    /**
     * 根据用户和店铺删除用户店铺角色
     * DELETE /api/v1/user-shop-roles/user/{userId}/shop/{shopId}
     */
    @DeleteMapping("/user/{userId}/shop/{shopId}")
    public Result<String> deleteUserShopRoleByUserAndShop(@PathVariable String userId, @PathVariable String shopId) {
        userShopRoleService.deleteUserShopRoleByUserAndShop(userId, shopId);
        return Result.success("用户店铺角色删除成功");
    }
}