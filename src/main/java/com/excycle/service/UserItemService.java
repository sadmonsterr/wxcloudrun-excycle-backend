package com.excycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.excycle.entity.Item;
import com.excycle.entity.User;
import com.excycle.entity.UserItem;
import com.excycle.mapper.ItemMapper;
import com.excycle.mapper.UserItemMapper;
import com.excycle.mapper.UserMapper;
import javax.annotation.PostConstruct;

import com.excycle.vo.ItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserItemService extends ServiceImpl<UserItemMapper, UserItem> {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private UserMapper userMapper;

    @PostConstruct
    public void init() {
        itemMapper.selectById("test");
    }

    public List<UserItem> queryUserItems(String userId) {
        List<Item> items = itemMapper.selectList(new LambdaQueryWrapper<>());
        List<String> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        LambdaQueryWrapper<UserItem> queryWrapper = new LambdaQueryWrapper<UserItem>()
                .eq(UserItem::getUserId, userId)
                .in(UserItem::getItemId, itemIds);
        Map<String, UserItem> userItemById = baseMapper.selectList(queryWrapper)
                .stream()
                .collect(Collectors.toMap(UserItem::getItemId, userItem -> userItem));
        return items.stream()
                .map(item -> userItemById.computeIfAbsent(item.getId(), s -> toUserItem(item)))
                .collect(Collectors.toList());
    }

    public List<ItemVO> queryUserItemsByOpenId(String openId) {
        List<Item> items = itemMapper.selectList(new LambdaQueryWrapper<>());
        List<String> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        LambdaQueryWrapper<UserItem> queryWrapper = new LambdaQueryWrapper<UserItem>()
                .eq(UserItem::getOpenid, openId)
                .in(UserItem::getItemId, itemIds);
        Map<String, UserItem> userItemById = baseMapper.selectList(queryWrapper)
                .stream()
                .collect(Collectors.toMap(UserItem::getItemId, userItem -> userItem));
        return items.stream()
                .map(item -> {
                    ItemVO itemVO = new ItemVO();
                    BeanUtils.copyProperties(item, itemVO);
                    userItemById.computeIfPresent(item.getId(), (s, userItem) -> {
                        itemVO.setPrice(userItem.getPrice());
                        return userItem;
                    });
                    return itemVO;
                }).collect(Collectors.toList());
    }

    private UserItem toUserItem(Item item) {
        UserItem userItem = new UserItem();
        userItem.setItemId(item.getId());
        userItem.setItemName(item.getName());
        userItem.setPrice(item.getPrice());
        return userItem;
    }

    public Page<UserItem> getUserItemPage(Page<UserItem> page, UserItem queryItem) {

        List<Item> items = itemMapper.selectList(new LambdaQueryWrapper<>());

        QueryWrapper<UserItem> queryWrapper = new QueryWrapper<>();

        if (queryItem.getUserId() != null) {
            queryWrapper.eq("userId", queryItem.getUserId());
        }
        if (queryItem.getItemId() != null) {
            queryWrapper.eq("itemId", queryItem.getItemId());
        }
        if (queryItem.getOwner() != null) {
            queryWrapper.eq("owner", queryItem.getOwner());
        }

        queryWrapper.orderByDesc("updatedAt");

        return this.page(page, queryWrapper);
    }

    public boolean saveUserItem(UserItem userItem) {
        return this.save(userItem);
    }

    public boolean updateUserItem(UserItem userItem) {
        return this.updateById(userItem);
    }

    public boolean deleteUserItem(String id) {
        return this.removeById(id);
    }

    public UserItem getUserItemByUserAndItem(String userId, String itemId) {
        QueryWrapper<UserItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("itemId", itemId);
        return this.getOne(queryWrapper);
    }

    public boolean updateUserItemPrice(String userId, String itemId, Double price) {
        String openId = userMapper.selectById(userId).getOpenId();
        return updateUserItemPrice(userId, openId, itemId, price);
    }

    public boolean updateUserItemPrice(String userId, String openId, String itemId, Double price) {
        QueryWrapper<UserItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("itemId", itemId);

        UserItem userItem = this.getOne(queryWrapper);
        if (userItem != null) {
            userItem.setPrice(price);
            return this.updateById(userItem);
        } else {
            userItem = new UserItem();
            userItem.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            userItem.setUserId(userId);
            userItem.setItemId(itemId);
            userItem.setOpenid(openId);
            userItem.setPrice(price);
            return this.save(userItem);
        }
    }

    public boolean batchUpdateUserItemPrices(String userId, Map<String, Double> itemPrices) {
        User user = userMapper.selectById(userId);
        for (Map.Entry<String, Double> entry : itemPrices.entrySet()) {
            String itemId = entry.getKey();
            Double price = entry.getValue();
            updateUserItemPrice(userId, user.getOpenId(), itemId, price);
        }
        return true;
    }
}