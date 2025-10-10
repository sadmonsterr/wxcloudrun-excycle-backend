package com.excycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.excycle.dto.ShopDTO;
import com.excycle.entity.Shop;
import com.excycle.mapper.ShopMapper;
import com.excycle.service.ShopService;
import com.excycle.utils.UUIDUtils;
import com.excycle.vo.ShopVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {


    @Override
    public Page<ShopVO> getShopPage(int page, int size, String name) {
        Page<Shop> shopPage = new Page<>(page, size);
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<Shop>()
                .eq(Shop::getDeleted, 0)
                .like(StringUtils.isNotBlank(name), Shop::getName, name)
                .orderByDesc(Shop::getId);

        Page<Shop> resultPage = page(shopPage, queryWrapper);

        Page<ShopVO> voPage = new Page<>();
        BeanUtils.copyProperties(resultPage, voPage);
        voPage.setRecords(resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList()));

        return voPage;
    }

    @Override
    public ShopVO getShopById(String id) {
        Shop shop = getBaseMapper().getByShopId(id);
        return shop != null ? convertToVO(shop) : null;
    }

    @Override
    public ShopVO createShop(ShopDTO shopDTO) {
        Shop shop = new Shop();
        BeanUtils.copyProperties(shopDTO, shop);
        if (shop.getShopId() == null) {
            shop.setShopId(UUIDUtils.nextBase62SnowflakeId());
        }
        shop.setDeleted(0L);
        save(shop);
        return convertToVO(shop);
    }

    @Override
    public ShopVO updateShop(String id, ShopDTO shopDTO) {
        Shop shop = baseMapper.getByShopId(id);
        if (shop == null) {
            return null;
        }
        BeanUtils.copyProperties(shopDTO, shop);
        updateById(shop);
        return convertToVO(shop);
    }

    @Override
    public boolean deleteShop(String id) {
        Shop shop = getBaseMapper().getByShopId(id);
        if (shop != null) {
            shop.setDeleted(1L);
            return updateById(shop);
        }
        return false;
    }

    private ShopVO convertToVO(Shop shop) {
        ShopVO vo = new ShopVO();
        BeanUtils.copyProperties(shop, vo);
        return vo;
    }
}