package com.merchant.ops.service;

import com.merchant.ops.dto.*;
import com.merchant.ops.result.PageResult;
import com.merchant.ops.vo.OrderPaymentVO;
import com.merchant.ops.vo.OrderSubmitVO;
import com.merchant.ops.vo.OrderVO;

public interface OrderService {
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);



    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);


    /*
    * 历史订单查询
    * @param ordersPageQueryDTO
     */
    PageResult pageQuery4User(int page, int pageSize, Integer status);

    /*
     *查询订单详情
     * @param id
     * @return
     */
    OrderVO getOrderDetail(Long id);

    /*
    * 用户取消订单
    * @param id
    * @return
     */
    void cancel(Long id);


    /*
     * 再来一单
     */
    void repetition(Long id);


    /*
     * 条件搜索订单
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
     * 统计订单数据
     */
    Object statistics();


    /*
     * 确认订单
     */
    void confirm(Long id);


    /*
     * 拒单
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);



    /**
     * 商家取消订单
     *
     * @param ordersCancelDTO
     */
    void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception;


    /*
     * 订单派送
     */
    void delivery(Long id);


    /*
     * 订单完成
     */
    void complete(Long id);


    /*
     * 订单催单
     */
    void reminder(Long id);
}
