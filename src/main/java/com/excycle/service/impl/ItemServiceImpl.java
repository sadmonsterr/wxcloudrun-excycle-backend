package com.excycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.excycle.entity.Item;
import com.excycle.mapper.ItemMapper;
import com.excycle.service.ItemService;
import org.springframework.stereotype.Service;

@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    @Override
    public Page<Item> getItemPage(Page<Item> page, Item item) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();

        if (item.getName() != null && !item.getName().isEmpty()) {
            queryWrapper.like(Item::getName, item.getName());
        }

        queryWrapper.orderByDesc(Item::getCreatedAt);

        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public boolean addItem(Item item) {
        return save(item);
    }

    @Override
    public boolean updateItem(Item item) {
        return updateById(item);
    }

    @Override
    public boolean deleteItem(Long id) {
        return removeById(id);
    }

    @Override
    public boolean updateItemPrice(String id, Double price) {
        Item item = getById(id);
        if (item != null) {
            item.setPrice(price);
            return updateById(item);
        }
        return false;
    }
}