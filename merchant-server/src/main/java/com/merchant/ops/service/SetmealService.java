package com.merchant.ops.service;

import com.merchant.ops.dto.SetmealDTO;
import com.merchant.ops.dto.SetmealPageQueryDTO;
import com.merchant.ops.entity.Setmeal;
import com.merchant.ops.entity.SetmealDish;
import com.merchant.ops.result.PageResult;
import com.merchant.ops.result.Result;
import com.merchant.ops.vo.DishItemVO;
import com.merchant.ops.vo.SetmealVO;

import java.util.List;


public interface SetmealService {
    /*
     * 新增套餐，同时增加套餐和菜品关系
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /*
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);


    /*
     * 批量删除
     * @param ids
     * @return
     */
    void deleteBatch(List<Long> ids);


    /*
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    void update(SetmealDTO setmealDTO);

    SetmealVO getByIdWithDish(Long id);



    /*
     * 启用、禁用套餐
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /*
     * 查询当前分类下的套餐
     */
    List<Setmeal> list(Setmeal setmeal);

    Result<List<SetmealDish>> getById(Long id);

    List<DishItemVO> getDishItemById(Long id);
}
