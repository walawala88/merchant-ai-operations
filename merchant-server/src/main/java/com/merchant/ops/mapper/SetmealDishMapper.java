package com.merchant.ops.mapper;

import com.merchant.ops.entity.Dish;
import com.merchant.ops.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /*
    *根据菜品id查询对应套餐id
    * @param dishId
    * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    void insertBatch(@Param("setmealDishList")List<SetmealDish> setmealDishes);

    void deleteBySetmealIds(List<Long> ids);


    /*
    * 根据套餐id删除套餐和菜品的关联数据
    * @param id
    * @return
     */
    void deleteBySetmealId(Long id);


    /*
    * 根据套餐id查询套餐和菜品的关联数据
    * @param id
    * @return
     */
    List<SetmealDish> getBySetmealId(Long id);


    /*
    * 根据套餐id查询套餐和菜品的关联数据
    * @param id
    * @return
     */
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getDishBySetmealId(Long id);
}
