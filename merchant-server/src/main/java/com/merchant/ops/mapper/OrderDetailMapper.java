package com.merchant.ops.mapper;

import com.merchant.ops.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单明细数据
     * @param orderDetailList
     */
    void insertBatch(@Param("list") List<OrderDetail> orderDetailList);
    /**
     * 根据订单id查询订单明细
     * @return
     */
    @Select("select * from order_detail where order_id = #{id}")
    List<OrderDetail> getByOrderId(Long orderId);

    /*
    * 根据订单id删除订单
     */
    void deleteByOrderId(Long id);
}
