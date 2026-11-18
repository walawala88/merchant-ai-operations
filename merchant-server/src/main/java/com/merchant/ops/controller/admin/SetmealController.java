package com.merchant.ops.controller.admin;

import com.merchant.ops.dto.SetmealDTO;
import com.merchant.ops.dto.SetmealPageQueryDTO;
import com.merchant.ops.mq.CacheInvalidationPublisher;
import com.merchant.ops.result.PageResult;
import com.merchant.ops.result.Result;
import com.merchant.ops.service.SetmealService;
import com.merchant.ops.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理。套餐缓存通过 RabbitMQ 或本地降级策略失效。
 */
@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CacheInvalidationPublisher cacheInvalidationPublisher;

    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        // 旧实现：@CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
        setmealService.saveWithDish(setmealDTO);
        cacheInvalidationPublisher.publishSetmealChanged();
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> Page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询：{}", setmealPageQueryDTO);
        return Result.success(setmealService.pageQuery(setmealPageQueryDTO));
    }

    @DeleteMapping
    @ApiOperation("批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        // 旧实现：@CacheEvict(cacheNames = "setmealCache", allEntries = true)
        log.info("批量删除：{}", ids);
        setmealService.deleteBatch(ids);
        cacheInvalidationPublisher.publishSetmealChanged();
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("查询套餐，用于页面显示")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        return Result.success(setmealService.getByIdWithDish(id));
    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        // 旧实现：@CacheEvict(cacheNames = "setmealCache", allEntries = true)
        log.info("修改套餐：{}", setmealDTO);
        setmealService.update(setmealDTO);
        cacheInvalidationPublisher.publishSetmealChanged();
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售停售")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        // 旧实现：@CacheEvict(cacheNames = "setmealCache", allEntries = true)
        setmealService.startOrStop(status, id);
        cacheInvalidationPublisher.publishSetmealChanged();
        return Result.success();
    }
}
