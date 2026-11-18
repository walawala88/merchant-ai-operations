package com.merchant.ops.service;

import com.merchant.ops.dto.ShoppingCartDTO;
import com.merchant.ops.entity.ShoppingCart;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ShoppingCartService {
    /*
     * 添加购物车
     * @param shoppingCartDTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    /*
     * 查看购物车
     */
    List<ShoppingCart> showShoppingCart();


    /*
     * 清空购物车
     */
    void clean();


    /*
     * 删除购物车
     */
   void sub(ShoppingCartDTO  shoppingCartDTO);
}
