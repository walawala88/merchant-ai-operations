package com.merchant.ops.controller.admin;

import com.merchant.ops.dto.OrdersCancelDTO;
import com.merchant.ops.dto.OrdersPageQueryDTO;
import com.merchant.ops.dto.OrdersRejectionDTO;
import com.merchant.ops.result.PageResult;
import com.merchant.ops.result.Result;
import com.merchant.ops.service.OrderService;
import com.merchant.ops.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "订单管理接口")
public class OrderController {
    @Autowired
    private OrderService orderService;
    /*
    *订单搜索
    * @param ordersPageQuery
    * @return
     */
    @RequestMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    /*
    *各个状态的订单数量统计
    * @return
     */
    @RequestMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result statistics(){
        log.info("各个状态的订单数量统计");
        return Result.success(orderService.statistics());
    }


    /*
    *订单详情
     */
    @RequestMapping("/details/{id}")
    @ApiOperation("订单详情")
    public Result<OrderVO> details(@PathVariable Long id){
        log.info("订单详情：{}", id);
        return Result.success(orderService.getOrderDetail(id));
    }

    /*
    *接单
    * @param id
    * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("商家接单")
    public Result confirm(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");  // 从JSON里拿id
        log.info("商家接单：{}", id);
        orderService.confirm(id);
        return Result.success();
    }


    /*
    *拒单
    * @param ordersRejectionDTO
    * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("商家拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("商家拒单：{}", ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /*
    *取消订单
     */
    /**
     * 取消订单
     *
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    /*
    *派送订单
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id){
        log.info("派送订单：{}", id);
        orderService.delivery(id);
        return Result.success();
    }


    /*
    *完成订单
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id){
        log.info("完成订单：{}", id);
        orderService.complete(id);
        return Result.success();
    }

}
