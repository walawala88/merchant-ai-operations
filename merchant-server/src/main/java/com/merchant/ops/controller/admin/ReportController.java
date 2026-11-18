package com.merchant.ops.controller.admin;

import com.merchant.ops.result.Result;
import com.merchant.ops.service.ReportService;
import com.merchant.ops.vo.SalesTop10ReportVO;
import com.merchant.ops.vo.TurnoverReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/admin/report")
@Api(tags = "数据统计相关接口")
public class ReportController {

    /*
    *营业额统计
     */
    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("营业额统计：{} 到 {}", begin, end);
        return Result.success(reportService.getTurnoverStatistics(begin, end));
    }

    /*
    *用户统计
     */
    @GetMapping("/userStatistics")
    public Result userStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("用户统计：{} 到 {}", begin, end);
        return Result.success(reportService.getUserStatistics(begin, end));
    }

    /*
    *订单统计
     */
    @GetMapping("/ordersStatistics")
    public Result ordersStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("订单统计：{} 到 {}", begin, end);
        return Result.success(reportService.getOrdersStatistics(begin, end));
    }

    /*
    *销量排名
     */
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("销量排名Top10：{} 到 {}", begin, end);
        return Result.success(reportService.getSalesTop10(begin, end));
    }

    @GetMapping("/export")
    @ApiOperation("导出运营数据报表")
    public void export(HttpServletResponse  response){
        reportService.exportBusinessData(response);
        log.info("导出数据成功");
    }

}