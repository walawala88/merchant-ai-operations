package com.merchant.ops.service;

import com.merchant.ops.dto.DishDTO;
import com.merchant.ops.dto.DishPageQueryDTO;
import com.merchant.ops.entity.Dish;
import com.merchant.ops.result.PageResult;
import com.merchant.ops.vo.DishVO;

import java.util.List;

/*
 * 新增菜品与对应口味
 * @param dishDTO
 */
public interface DishService {
    List<DishVO> listWithFlavor(Dish dish);

    /*
     * 菜品查询
     * @param dish
     * @return
     */


    public void saveWithFlavor(DishDTO dishDTO);


    /*
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);


    /*
     * 批量删除
     * @param ids
     * @return
     */
    void deleteBatch(List<Long> ids);


    /*
     *根据id查询菜品和对应的口味
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);


    /*
     * 根据id修改菜品基本信息和对应口味信息
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

/*
*根据分类id查询菜品
* @param categoryId
* @return
 */
    List<DishVO> list(Long categoryId);


    /*
     * 启用、禁用分类
     * @param status
     * @param id
     * @return
     */
    void startOrStop(Integer status, Long id);
}
