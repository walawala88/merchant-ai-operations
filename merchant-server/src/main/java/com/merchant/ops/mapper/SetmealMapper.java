package com.merchant.ops.mapper;

import com.github.pagehelper.Page;
import com.merchant.ops.dto.SetmealPageQueryDTO;
import com.merchant.ops.entity.Setmeal;
import com.merchant.ops.vo.DishItemVO;
import com.merchant.ops.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    void insert(Setmeal seatmeal);

    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    Setmeal getById(Long id);

    void deleteBatch(List<Long> ids);

    /*
    * 修改套餐
    * @param setmeal
    * @return
     */
    void update(Setmeal setmeal);

    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);


    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

}
