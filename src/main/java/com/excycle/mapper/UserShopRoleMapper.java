package com.excycle.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.excycle.entity.UserShopRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserShopRoleMapper extends BaseMapper<UserShopRole> {

    @Update("update user_shop_role set deleted = #{deletedTime} where id = #{id} and deleted = 0")
    boolean deleteById(@Param("id") String id, @Param("deletedTime") Long deletedTime);


    @Update("update user_shop_role set deleted = #{deletedTime} where user_id = #{userId} and shop_id = #{shopId} and deleted = 0")
    boolean deletedByUserAndShop(@Param("userId") String userId, @Param("shopId") String shopId, @Param("deletedTime") Long deletedTime);

    default UserShopRole getByUserId(String userId) {
        LambdaQueryWrapper<UserShopRole> queryWrapper = new LambdaQueryWrapper<UserShopRole>()
                .eq(UserShopRole::getUserId, userId)
                .eq(UserShopRole::getDeleted, 0);
        return this.selectOne(queryWrapper);
    }

    default UserShopRole getByShopBoss(String shopId) {
        LambdaQueryWrapper<UserShopRole> queryWrapper = new LambdaQueryWrapper<UserShopRole>()
                .eq(UserShopRole::getShopId, shopId)
                .eq(UserShopRole::getRoleId, "boss")
                .eq(UserShopRole::getDeleted, 0);
        return this.selectOne(queryWrapper);
    }
}