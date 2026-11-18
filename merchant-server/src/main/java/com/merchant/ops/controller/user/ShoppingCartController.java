package com.merchant.ops.controller.user;

import com.merchant.ops.dto.ShoppingCartDTO;
import com.merchant.ops.entity.ShoppingCart;
import com.merchant.ops.result.Result;
import com.merchant.ops.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端-购物车接口")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    /*
    * 添加购物车
     */
    @RequestMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车:{}", shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }
    /*
    * 查看购物车
     */
    @RequestMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list(){
       List<ShoppingCart> list = shoppingCartService.showShoppingCart();
       return Result.success(list);
    }

    /*
    * 清空购物车
     */
    @RequestMapping("/clean")
    @ApiOperation("清空购物车")
    public Result clean(){
        shoppingCartService.clean();
        return Result.success();
    }

    /*
    *删除购物车
    * @param ids
     */
    @RequestMapping("/sub")
    @ApiOperation("购物车减少数量/删除")
    public Result sub(@RequestBody ShoppingCartDTO  shoppingCartDTO){
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }
}
