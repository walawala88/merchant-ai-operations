package com.merchant.ops.mapper;

import com.merchant.ops.dto.ShoppingCartDTO;
import com.merchant.ops.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

/*
* 修改购物车
 */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart cart);
    /*
    * 查询购物车
     */

    List<ShoppingCart> list(ShoppingCart shoppingCart);
    /*
    * 添加购物车
     */
    @Insert("insert into shopping_cart (name, image, dish_id, setmeal_id, dish_flavor, number, amount, create_time, user_id) " +
            "values (#{name}, #{image}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime}, #{userId})")
    void insert(ShoppingCart shoppingCart);


    /*
    * 清空购物车
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /*
    *删除购物车
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);
}
