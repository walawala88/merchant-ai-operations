package com.merchant.ops.service.impl;

import com.merchant.ops.dto.GoodsSalesDTO;
import com.merchant.ops.entity.Orders;
import com.merchant.ops.mapper.OrderMapper;
import com.merchant.ops.mapper.UserMapper;
import com.merchant.ops.service.ReportService;
import com.merchant.ops.service.WorkspaceService;
import com.merchant.ops.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;


    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }


    /*
    *用户统计
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 查总用户
            Map<String, Object> map = new HashMap<>();
            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);

            // 查新增用户
            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);

            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }


    /*
    *订单统计
     */
    @Override
    public Object getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);

            // 每日订单总数
            Integer orderCount = orderMapper.countByMap(map);
            // 强制保证不为null，没数据就是0
            orderCount = orderCount == null ? 0 : orderCount;
            orderCountList.add(orderCount);

            // 每日有效订单
            map.put("status", Orders.COMPLETED);
            Integer validCount = orderMapper.countByMap(map);
            validCount = validCount == null ? 0 : validCount;
            validOrderCountList.add(validCount);
        }

        // 统计总数
        int totalOrderCount = orderCountList.stream().mapToInt(Integer::intValue).sum();
        int validOrderCount = validOrderCountList.stream().mapToInt(Integer::intValue).sum();
        double orderCompletionRate = totalOrderCount == 0 ? 0.0 : (double) validOrderCount / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(dateList.stream().map(LocalDate::toString).collect(Collectors.joining(",")))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .orderCompletionRate(orderCompletionRate)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        //通过stream获取对象
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        //以，分割
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names, ","))
                .numberList(StringUtils.join(numbers, ","))
                .build();

    }


    /*
    *导出营业数据
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //查询数据库中营业数据
        LocalDate databegin = LocalDate.now().minusDays(30);
        LocalDate dataend = LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(databegin, LocalTime.MIN), LocalDateTime.of(dataend, LocalTime.MAX));

        try {
            // --------------- 这里是修复核心 ---------------
            // 读取你项目中的 Excel 模板文件，而不是新建空文件
            XSSFWorkbook excel = new XSSFWorkbook(
                    this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx")
            );

            // 从模板中获取 sheet（现在一定不为 null）
            XSSFSheet sheet = excel.getSheet("sheet1");

            // 填充时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + databegin + " 至 " + dataend);

            // 第3行：概览数据
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            // 第4行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            // 填充30天明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = databegin.plusDays(i);
                BusinessDataVO businessData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX)
                );

                XSSFRow dataRow = sheet.getRow(7 + i);
                dataRow.getCell(1).setCellValue(date.toString());
                dataRow.getCell(2).setCellValue(businessData.getTurnover());
                dataRow.getCell(3).setCellValue(businessData.getValidOrderCount());
                dataRow.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                dataRow.getCell(5).setCellValue(businessData.getUnitPrice());
                dataRow.getCell(6).setCellValue(businessData.getNewUsers());
            }

            // 输出到浏览器
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=businessData.xlsx");
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            out.flush();
            out.close();
            excel.close();

        } catch (Exception e) {
            log.error("操作失败", e);
        }



    }


}