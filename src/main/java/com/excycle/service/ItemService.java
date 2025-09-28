package com.excycle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.excycle.entity.Item;

public interface ItemService extends IService<Item> {

    Page<Item> getItemPage(Page<Item> page, Item item);

    boolean addItem(Item item);

    boolean updateItem(Item item);

    boolean deleteItem(Long id);

    boolean updateItemPrice(String id, Double price);
}