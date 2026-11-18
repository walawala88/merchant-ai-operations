package com.merchant.ops.controller.user;

import com.merchant.ops.dto.OrdersPageQueryDTO;
import com.merchant.ops.dto.OrdersPaymentDTO;
import com.merchant.ops.dto.OrdersSubmitDTO;
import com.merchant.ops.result.PageResult;
import com.merchant.ops.result.Result;
import com.merchant.ops.service.OrderService;
import com.merchant.ops.vo.OrderPaymentVO;
import com.merchant.ops.vo.OrderSubmitVO;
import com.merchant.ops.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "C端订单接口")
public class OrderController {
    @Autowired
    private OrderService orderService;


    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @RequestMapping("/submit")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        OrderSubmitVO orderSubmitVO =  orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
//    @PutMapping("/payment")
//    @ApiOperation("订单支付")
//    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        log.info("订单支付：{}", ordersPaymentDTO);
//        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
//        log.info("生成预支付交易单：{}", orderPaymentVO);
//        return Result.success(orderPaymentVO);
//    }

    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<String> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return  Result.success();
    }
/*
*查询历史订单
* @param page
* @param pageSize
* @return
 */
    @GetMapping("/historyOrders")
    public Result<PageResult> list(int page, int pageSize, Integer status){
        PageResult pageResult = orderService.pageQuery4User(page, pageSize, status);
        return Result.success(pageResult);
    }

    /*
     *查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id){
        OrderVO orderVO = orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }

    /*
    * 取消订单
    * @param id
    * @return
     */
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id){
        orderService.cancel(id);
        return Result.success();
    }

    /*
    *再来一单
    * @param id
    * @return
     */
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id){
        orderService.repetition(id);
        return Result.success();
    }

    /*
    *用户催单
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单")
    public Result reminder(@PathVariable Long id){
        orderService.reminder(id);
        return Result.success();
    }

}
