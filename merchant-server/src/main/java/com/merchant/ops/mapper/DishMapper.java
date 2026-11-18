package com.merchant.ops.mapper;

import com.github.pagehelper.Page;
import com.merchant.ops.annotation.AutoFill;
import com.merchant.ops.dto.DishPageQueryDTO;
import com.merchant.ops.entity.Dish;
import com.merchant.ops.enumeration.OperationType;
import com.merchant.ops.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);
    /*
    * 插入菜品数据
    * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);
    /*
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);


    /*
     * 根据主键查询菜品
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);


    /*
     * 根据主键删除菜品
     * @param id
     * @return
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    /*
     * 根据菜品id批量删除菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);


    /*
     * 根据id动态修改菜品数据
     * @param dish
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    List<Dish> list(Dish dish);


    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
