package com.excycle.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.excycle.context.UserContext;
import com.excycle.entity.UserItem;
import com.excycle.vo.ItemVO;
import com.excycle.vo.UserItemVO;
import com.excycle.service.UserItemService;
import com.excycle.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户物品管理RESTful API
 */
@RestController
@RequestMapping("/api/v1/user-items")
public class UserItemApiController {

    @Autowired
    private UserItemService userItemService;

    /**
     * 获取用户物品列表
     * GET /api/v1/user-items?page=1&size=10&userId=123&itemId=456
     */
    @GetMapping
    public Result<Page<UserItemVO>> getUserItems(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String itemId,
            @RequestParam(required = false) String owner) {

        UserItem queryUserItem = new UserItem();
        if (userId != null) queryUserItem.setUserId(userId);
        if (itemId != null) queryUserItem.setItemId(itemId);
        if (owner != null) queryUserItem.setOwner(owner);

        Page<UserItem> userItemPage = userItemService.getUserItemPage(new Page<>(page, size), queryUserItem);
        // 转换为VO
        Page<UserItemVO> userItemVOPage = new Page<>();
        userItemVOPage.setCurrent(userItemPage.getCurrent());
        userItemVOPage.setSize(userItemPage.getSize());
        userItemVOPage.setTotal(userItemPage.getTotal());
        userItemVOPage.setRecords(userItemPage.getRecords().stream().map(this::convertToUserItemVO) .collect(Collectors.toList()));
        return Result.success(userItemVOPage);
    }

    private UserItemVO convertToUserItemVO(UserItem userItem) {
        UserItemVO userItemVO = new UserItemVO();
        userItemVO.setId(userItem.getId());
        userItemVO.setOwner(userItem.getOwner());
        userItemVO.setItemName(userItem.getItemName());
        userItemVO.setItemId(userItem.getItemId());
        userItemVO.setUserId(userItem.getUserId());
        userItemVO.setOpenid(userItem.getOpenid());
        userItemVO.setPrice(userItem.getPrice());
        userItemVO.setCreatedAt(userItem.getCreatedAt());
        userItemVO.setUpdatedAt(userItem.getUpdatedAt());
        return userItemVO;
    }

    /**
     * 根据ID获取用户物品
     * GET /api/v1/user-items/{id}
     */
    @GetMapping("/{id}")
    public Result<UserItemVO> getUserItemById(@PathVariable String id) {
        UserItem userItem = userItemService.getById(id);
        if (userItem != null) {
            return Result.success(convertToUserItemVO(userItem));
        } else {
            return Result.error("用户物品不存在");
        }
    }

    /**
     * 根据用户ID和物品ID获取用户物品
     * GET /api/v1/user-items/user/{userId}/item/{itemId}
     */
    @GetMapping("/user/{userId}/item/{itemId}")
    public Result<UserItemVO> getUserItemByUserAndItem(@PathVariable String userId, @PathVariable String itemId) {
        UserItem userItem = userItemService.getUserItemByUserAndItem(userId, itemId);
        if (userItem != null) {
            return Result.success(convertToUserItemVO(userItem));
        } else {
            return Result.error("用户物品不存在");
        }
    }

    @GetMapping("/user/{userId}")
    public Result<List<UserItemVO>> queryUserItems(@PathVariable String userId) {
        List<UserItem> userItems = userItemService.queryUserItems(userId);
        List<UserItemVO> userItemVOs = userItems.stream().map(this::convertToUserItemVO) .collect(Collectors.toList());
        return Result.success(userItemVOs);
    }

    @GetMapping("/open")
    public Result<Page<ItemVO>> queryUserItemsByOpenId() {
        List<ItemVO> userItems = userItemService.queryUserItemsByOpenId(UserContext.getCurrentOpenId());
        Page<ItemVO> page = new Page<>();
        page.setTotal(userItems.size());
        page.setRecords(userItems);
        return Result.success(page);
    }

    /**
     * 创建用户物品
     * POST /api/v1/user-items
     */
    @PostMapping
    public Result<String> createUserItem(@RequestBody UserItem userItem) {
        boolean success = userItemService.saveUserItem(userItem);
        return Result.success( userItem.getId());
    }

    /**
     * 更新用户物品
     * POST /api/v1/user-items/{id}
     */
    @PostMapping("/{id}")
    public Result<String> updateUserItem(@PathVariable String id, @RequestBody UserItem userItem) {
        userItem.setId(id);
        boolean success = userItemService.updateUserItem(userItem);
        return Result.success("用户物品更新成功", id);
    }

    /**
     * 更新用户物品价格
     * POST /api/v1/user-items/user/{userId}/item/{itemId}/price
     */
    @PostMapping("/user/{userId}/item/{itemId}/price")
    public Result<String> updateUserItemPrice(
            @PathVariable String userId,
            @PathVariable String itemId,
            @RequestBody UserItemPriceUpdateRequest request) {
        boolean success = userItemService.updateUserItemPrice(userId, itemId, request.getPrice());
        UserItem userItem = userItemService.getUserItemByUserAndItem(userId, itemId);
        return Result.success("价格更新成功", userItem.getId());
    }

    /**
     * 批量更新用户物品价格
     * POST /api/v1/user-items/user/{userId}/prices
     */
    @PostMapping("/user/{userId}/prices")
    public Result<String> batchUpdateUserItemPrices(
            @PathVariable String userId,
            @RequestBody BatchUserItemPriceUpdateRequest request) {
        boolean success = userItemService.batchUpdateUserItemPrices(userId, request.getItemPrices());
        return Result.success("批量价格更新成功");
    }

    /**
     * 删除用户物品
     * DELETE /api/v1/user-items/{id}
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUserItem(@PathVariable String id) {
        boolean success = userItemService.deleteUserItem(id);
        if (success) {
            return Result.success("用户物品删除成功");
        } else {
            return Result.error("用户物品删除失败");
        }
    }

    /**
     * 批量删除用户物品
     * DELETE /api/v1/user-items?ids=1,2,3
     */
    @DeleteMapping
    public Result<String> batchDeleteUserItems(@RequestParam List<String> ids) {
        boolean success = userItemService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        } else {
            return Result.error("批量删除失败");
        }
    }

    /**
     * 获取用户物品总数
     * GET /api/v1/user-items/count
     */
    @GetMapping("/count")
    public Result<Long> getUserItemCount() {
        long count = userItemService.count();
        return Result.success(count);
    }

    /**
     * 价格更新请求DTO
     */
    public static class UserItemPriceUpdateRequest {
        private Double price;

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }
    }

    /**
     * 批量价格更新请求DTO
     */
    public static class BatchUserItemPriceUpdateRequest {
        private Map<String, Double> itemPrices;

        public Map<String, Double> getItemPrices() {
            return itemPrices;
        }

        public void setItemPrices(Map<String, Double> itemPrices) {
            this.itemPrices = itemPrices;
        }
    }
}