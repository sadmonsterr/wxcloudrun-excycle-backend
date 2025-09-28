package com.excycle.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.excycle.entity.Item;
import com.excycle.service.ItemService;
import com.excycle.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物品管理RESTful API
 */
@RestController
@RequestMapping("/api/v1/items")
public class ItemApiController {

    @Autowired
    private ItemService itemService;

    /**
     * 获取物品列表
     * GET /api/v1/items?page=1&size=10&name=test&category=electronics
     */
    @GetMapping
    public Result<Page<Item>> getItems(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active) {

        Item queryItem = new Item();
        if (name != null) queryItem.setName(name);

        Page<Item> itemPage = itemService.getItemPage(new Page<>(page, size), queryItem);
        return Result.success(itemPage);
    }

    /**
     * 根据ID获取物品
     * GET /api/v1/items/{id}
     */
    @GetMapping("/{id}")
    public Result<Item> getItemById(@PathVariable String id) {
        Item item = itemService.getById(id);
        if (item != null) {
            return Result.success(item);
        } else {
            return Result.error("物品不存在");
        }
    }

    /**
     * 创建物品
     * POST /api/v1/items
     */
    @PostMapping
    public Result<Item> createItem(@RequestBody Item item) {
        boolean success = itemService.addItem(item);
        if (success) {
            return Result.success("物品创建成功", item);
        } else {
            return Result.error("物品创建失败");
        }
    }

    /**
     * 更新物品
     * PUT /api/v1/items/{id}
     */
    @PutMapping("/{id}")
    public Result<Item> updateItem(@PathVariable String id, @RequestBody Item item) {
        item.setId(id);
        boolean success = itemService.updateItem(item);
        if (success) {
            return Result.success("物品更新成功", item);
        } else {
            return Result.error("物品更新失败");
        }
    }

    /**
     * 更新物品价格
     * PATCH /api/v1/items/{id}/price
     */
    @PatchMapping("/{id}/price")
    public Result<Item> updateItemPrice(@PathVariable String id, @RequestBody PriceUpdateRequest request) {
        boolean success = itemService.updateItemPrice(id, request.getPrice());
        if (success) {
            Item item = itemService.getById(id);
            return Result.success("价格更新成功", item);
        } else {
            return Result.error("价格更新失败");
        }
    }

    /**
     * 部分更新物品
     * PATCH /api/v1/items/{id}
     */
    @PatchMapping("/{id}")
    public Result<Item> patchItem(@PathVariable String id, @RequestBody Item item) {
        item.setId(id);
        boolean success = itemService.updateItem(item);
        if (success) {
            return Result.success("物品更新成功", item);
        } else {
            return Result.error("物品更新失败");
        }
    }

    /**
     * 删除物品
     * DELETE /api/v1/items/{id}
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteItem(@PathVariable Long id) {
        boolean success = itemService.deleteItem(id);
        if (success) {
            return Result.success("物品删除成功");
        } else {
            return Result.error("物品删除失败");
        }
    }

    /**
     * 批量删除物品
     * DELETE /api/v1/items?ids=1,2,3
     */
    @DeleteMapping
    public Result<String> batchDeleteItems(@RequestParam List<Long> ids) {
        boolean success = itemService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        } else {
            return Result.error("批量删除失败");
        }
    }

    /**
     * 获取物品总数
     * GET /api/v1/items/count
     */
    @GetMapping("/count")
    public Result<Long> getItemCount() {
        long count = itemService.count();
        return Result.success(count);
    }

    /**
     * 价格更新请求DTO
     */
    public static class PriceUpdateRequest {
        private Double price;

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }
    }
}