package com.merchant.ops.mapper;

import com.github.pagehelper.Page;
import com.merchant.ops.dto.GoodsSalesDTO;
import com.merchant.ops.dto.OrdersPageQueryDTO;
import com.merchant.ops.entity.Orders;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.core.annotation.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /*
    * 插入订单数据
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);


    /*
    * 分页查询订单数据
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);


    /*
    * 各个状态的订单数量统计
     */
    Integer countStatus(Integer toBeConfirmed);

    /*
    * 查询指定时间区间内订单数量
     */
    @Select("select * from orders where order_time < #{orderTime} and status = #{status}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);


    /*
    * 统计营业额数据
     */
    Double sumByMap(Map map);


    /*
    * 统计用户数量
     */
    Integer countByMap(Map<String, Object> map);






    /*
    * 查询top10
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
