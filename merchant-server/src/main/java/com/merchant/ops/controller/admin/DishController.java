package com.merchant.ops.controller.admin;

import com.merchant.ops.dto.DishDTO;
import com.merchant.ops.dto.DishPageQueryDTO;
import com.merchant.ops.mq.CacheInvalidationPublisher;
import com.merchant.ops.result.PageResult;
import com.merchant.ops.result.Result;
import com.merchant.ops.service.DishService;
import com.merchant.ops.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理。
 *
 * 缓存失效不再由 Controller 同步删除 Redis，而是发布缓存失效事件：
 * RabbitMQ 开启时由消费者异步处理；关闭时由发布器本地降级处理。
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CacheInvalidationPublisher cacheInvalidationPublisher;

    // 旧实现：直接注入 RedisTemplate 并同步删除缓存
    // @Autowired
    // private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        cacheInvalidationPublisher.publishDishChanged(dishDTO.getCategoryId());
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> Page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询：{}", dishPageQueryDTO);
        return Result.success(dishService.pageQuery(dishPageQueryDTO));
    }

    @DeleteMapping
    @ApiOperation("批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除：{}", ids);
        dishService.deleteBatch(ids);
        cacheInvalidationPublisher.publishAllChanged();
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getDishById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        return Result.success(dishService.getByIdWithFlavor(id));
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        cacheInvalidationPublisher.publishAllChanged();
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);
        return Result.success(dishService.list(categoryId));
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);
        cacheInvalidationPublisher.publishAllChanged();
        return Result.success();
    }

    /*
     * 旧实现逻辑已停用，保留说明：
     * String key = "dish_" + dishDTO.getCategoryId();
     * redisTemplate.delete(key);
     *
     * Set keys = redisTemplate.keys("dish_*");
     * redisTemplate.delete(keys);
     */
}
