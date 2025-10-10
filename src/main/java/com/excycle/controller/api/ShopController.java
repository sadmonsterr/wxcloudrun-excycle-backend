package com.excycle.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.excycle.common.Result;
import com.excycle.dto.ShopDTO;
import com.excycle.service.ShopService;
import com.excycle.vo.ShopVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/shops")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @Data
    public static class ShopQueryRequest {
        private int page = 1;
        private int size = 10;
        private String name;
    }

    /**
     * 获取店铺列表
     * POST /api/v1/shops/list
     */
    @PostMapping("/list")
    public Result<Page<ShopVO>> getShops(@RequestBody ShopQueryRequest queryRequest) {
        return Result.success(shopService.getShopPage(queryRequest.getPage(), queryRequest.getSize(), queryRequest.getName()));
    }

    /**
     * 根据ID获取店铺
     * GET /api/v1/shops/{id}
     */
    @GetMapping("/{id}")
    public Result<ShopVO> getShop(@PathVariable String id) {
        return Result.success(shopService.getShopById(id));
    }

    /**
     * 创建店铺
     * POST /api/v1/shops
     */
    @PostMapping
    public Result<ShopVO> createShop(@Validated @RequestBody ShopDTO shopDTO) {
        return Result.success("店铺创建成功", shopService.createShop(shopDTO));
    }

    /**
     * 更新店铺
     * POST /api/v1/shops/{id}
     */
    @PostMapping("/{id}")
    public Result<ShopVO> updateShop(@PathVariable String id, @Validated @RequestBody ShopDTO shopDTO) {
        return Result.success("店铺更新成功", shopService.updateShop(id, shopDTO));
    }

    /**
     * 删除店铺
     * DELETE /api/v1/shops/{id}
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteShop(@PathVariable String id) {
        shopService.deleteShop(id);
        return Result.success("店铺删除成功");
    }

    /**
     * 批量删除店铺
     * DELETE /api/v1/shops
     */
    @DeleteMapping
    public Result<String> batchDeleteShops(@RequestParam Map<String, Object> params) {
        // TODO: 实现批量删除逻辑
        return Result.success("批量删除成功");
    }
}