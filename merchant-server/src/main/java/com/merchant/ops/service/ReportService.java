package com.merchant.ops.service;

import com.merchant.ops.vo.SalesTop10ReportVO;
import com.merchant.ops.vo.TurnoverReportVO;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public interface ReportService {
    /**
     * 统计指定时间区间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);


    /*
    * 统计指定时间区间内的用户数据
     */
    Object getUserStatistics(LocalDate begin, LocalDate end);


    /*
    * 统计指定时间区间内的订单数据
     */
    Object getOrdersStatistics(LocalDate begin, LocalDate end);


    /*
    * 获取指定时间区间内的销量排名
     */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);


    /*
    * 导出营业数据
     */
    void exportBusinessData(HttpServletResponse response);
}
