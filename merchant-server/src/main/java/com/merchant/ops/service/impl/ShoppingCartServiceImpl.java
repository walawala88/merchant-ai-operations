package com.merchant.ops.service.impl;

import com.merchant.ops.context.BaseContext;
import com.merchant.ops.dto.ShoppingCartDTO;
import com.merchant.ops.entity.Dish;
import com.merchant.ops.entity.Setmeal;
import com.merchant.ops.entity.ShoppingCart;
import com.merchant.ops.mapper.DishMapper;
import com.merchant.ops.mapper.SetmealMapper;
import com.merchant.ops.mapper.ShoppingCartMapper;
import com.merchant.ops.result.Result;
import com.merchant.ops.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.beans.beancontext.BeanContext;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /*
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //如果存在，则数量加1
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);

        }else{
            // 不存在，需要查询菜品/套餐信息
            Long dishId = shoppingCart.getDishId(); // 这里只拿 ID
            if (dishId != null) {
                // 1. 根据 dishId 查询 完整的菜品对象
                Dish dish = dishMapper.getById(dishId); // ✅ 正确！

                // 2. 赋值名称、图片、价格
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            } else {
                // 套餐逻辑
                Long setmealId = shoppingCart.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());

            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }


    }

    /*
     * 查看购物车
     * @return
     */

    public List<ShoppingCart> showShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart =ShoppingCart.builder()
                .userId(userId)
                .build();
        return shoppingCartMapper.list(shoppingCart);
    }
/*
清空购物车
 */
    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }


    /*
    * 删除购物车
     */
    @Override
    public void sub(ShoppingCartDTO  shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            Integer number = cart.getNumber();
            if(number == 1){
                shoppingCartMapper.deleteById(cart.getId());
            }else{
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(cart);
            }

        }
    }
}
