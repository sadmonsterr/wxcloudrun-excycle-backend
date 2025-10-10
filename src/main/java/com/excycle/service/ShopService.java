package com.excycle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.excycle.dto.ShopDTO;
import com.excycle.entity.Shop;
import com.excycle.vo.ShopVO;

public interface ShopService extends IService<Shop> {
    Page<ShopVO> getShopPage(int page, int size, String name);

    ShopVO getShopById(String id);

    ShopVO createShop(ShopDTO shopDTO);

    ShopVO updateShop(String id, ShopDTO shopDTO);

    boolean deleteShop(String id);
}