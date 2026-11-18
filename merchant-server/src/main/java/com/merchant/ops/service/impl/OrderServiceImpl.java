package com.merchant.ops.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.merchant.ops.constant.MessageConstant;
import com.merchant.ops.context.BaseContext;
import com.merchant.ops.dto.*;
import com.merchant.ops.entity.*;
import com.merchant.ops.exception.OrderBusinessException;
import com.merchant.ops.mapper.*;
import com.merchant.ops.result.PageResult;
import com.merchant.ops.service.OrderService;
import com.merchant.ops.vo.OrderPaymentVO;
import com.merchant.ops.vo.OrderStatisticsVO;
import com.merchant.ops.vo.OrderSubmitVO;
import com.merchant.ops.vo.OrderVO;
import com.merchant.ops.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new RuntimeException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new RuntimeException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPayMethod(1); // 修复：支付方式赋值

        orders.setAddress(
                addressBook.getProvinceName() +
                        addressBook.getCityName() +
                        addressBook.getDistrictName() +
                        addressBook.getDetail()
        );
        orders.setPackAmount(0);
        orders.setTablewareNumber(1);
        orders.setTablewareStatus(1);
        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        shoppingCartMapper.deleteByUserId(userId);

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    /**
     * 订单支付
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        throw new OrderBusinessException("功能未开启");
    }

    /**
     * 支付成功，修改订单状态
     */
    public void paySuccess(String outTradeNo) {
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        if (ordersDB == null) return;

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);


        /*
         *来单提醒
         */
        //通过websocket向商家发送消息
        HashMap hashMap = new HashMap();
        hashMap.put("type", 1);
        hashMap.put("orderId", ordersDB.getId());
        hashMap.put("content", "来单提醒");
        webSocketServer.sendToAllClient(JSONObject.toJSONString(hashMap));
    }

    /**
     * 历史订单查询
     */
    @Override
    public PageResult pageQuery4User(int page, int pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);
        OrdersPageQueryDTO dto = new OrdersPageQueryDTO();
        dto.setUserId(BaseContext.getCurrentId());
        dto.setStatus(status);
        Page<Orders> pageObj = orderMapper.pageQuery(dto);

        List<OrderVO> list = new ArrayList<>();
        for (Orders orders : pageObj) {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(orders, vo);
            vo.setOrderDetailList(orderDetailMapper.getByOrderId(orders.getId()));
            list.add(vo);
        }
        return new PageResult(pageObj.getTotal(), list);
    }

    /**
     * 查询订单详情
     */
    @Override
    public OrderVO getOrderDetail(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(orders, vo);
        vo.setOrderDetailList(orderDetailMapper.getByOrderId(id));
        return vo;
    }

    /**
     * 用户取消订单
     */
    @Transactional
    @Override
    public void cancel(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 只允许 待付款 取消
        if (!ordersDB.getStatus().equals(Orders.PENDING_PAYMENT)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        ordersDB.setStatus(Orders.CANCELLED);
        ordersDB.setCancelReason("用户取消");
        ordersDB.setCancelTime(LocalDateTime.now());
        orderMapper.update(ordersDB);

        // ✅ 修复：取消订单 不删购物车
        // shoppingCartMapper.deleteByUserId(...) 这行彻底删除！

        redisTemplate.delete("order_" + id);
    }

    /**
     * 再来一单
     */
    @Transactional
    @Override
    public void repetition(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        List<OrderDetail> list = orderDetailMapper.getByOrderId(id);
        if (list == null || list.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();
        for (OrderDetail d : list) {
            ShoppingCart cart = ShoppingCart.builder()
                    .userId(userId)
                    .dishId(d.getDishId())
                    .setmealId(d.getSetmealId())
                    .name(d.getName())
                    .image(d.getImage())
                    .dishFlavor(d.getDishFlavor())
                    .number(d.getNumber())
                    .amount(d.getAmount())
                    .createTime(LocalDateTime.now())
                    .build();
            shoppingCartMapper.insert(cart);
        }
    }

    /**
     * 条件搜索
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(dto);
        List<OrderVO> voList = getOrderVOList(page);
        return new PageResult(page.getTotal(), voList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        List<OrderVO> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(page.getResult())) return list;

        for (Orders o : page.getResult()) {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(o, vo);
            vo.setOrderDishes(getOrderDishesStr(o));
            list.add(vo);
        }
        return list;
    }

    private String getOrderDishesStr(Orders orders) {
        List<OrderDetail> details = orderDetailMapper.getByOrderId(orders.getId());
        return details.stream()
                .map(d -> d.getName() + "*" + d.getNumber() + ";")
                .collect(Collectors.joining());
    }

    /**
     * 订单统计
     */
    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO vo = new OrderStatisticsVO();
        vo.setToBeConfirmed(orderMapper.countStatus(Orders.TO_BE_CONFIRMED));
        vo.setConfirmed(orderMapper.countStatus(Orders.CONFIRMED));
        vo.setDeliveryInProgress(orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS));
        return vo;
    }

    /**
     * 商家接单
     */
    @Transactional
    @Override
    public void confirm(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);

        // ✅ 修复：只删当前订单缓存
        redisTemplate.delete("order_" + id);
    }

    /**
     * 商家拒单
     */
    @Transactional
    @Override
    public void rejection(OrdersRejectionDTO dto) {
        Long orderId = dto.getId();
        Orders orders = orderMapper.getById(orderId);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(dto.getRejectionReason());
        orderMapper.update(orders);

        redisTemplate.delete("order_" + orderId);
    }

    /**
     * 管理端取消订单（带退款）
     */
    @Transactional
    @Override
    public void cancel(OrdersCancelDTO dto) {
        Long orderId = dto.getId();
        Orders ordersDB = orderMapper.getById(orderId);

        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 只能取消：待付款 / 待确认
        if (!ordersDB.getStatus().equals(Orders.PENDING_PAYMENT) &&
                !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        ordersDB.setStatus(Orders.CANCELLED);
        ordersDB.setCancelReason(dto.getCancelReason());
        ordersDB.setCancelTime(LocalDateTime.now());
        orderMapper.update(ordersDB);

        redisTemplate.delete("order_" + orderId);
    }

    /**
     * 派送订单
     */
    @Transactional
    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
        redisTemplate.delete("order_" + id);
    }

    /**
     * 完成订单
     */
    @Transactional
    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
        redisTemplate.delete("order_" + id);
    }


    /*
     * 订单催单
     */
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Map map = new HashMap();
        map.put("orderId", id);
        map.put("type", 2);
        map.put("content", "订单号：" + orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));


    }


}