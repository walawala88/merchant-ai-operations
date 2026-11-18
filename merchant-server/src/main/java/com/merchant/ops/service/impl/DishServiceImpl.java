package com.merchant.ops.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.merchant.ops.constant.MessageConstant;
import com.merchant.ops.constant.StatusConstant;
import com.merchant.ops.dto.DishDTO;
import com.merchant.ops.dto.DishPageQueryDTO;
import com.merchant.ops.entity.Dish;
import com.merchant.ops.entity.DishFlavor;
import com.merchant.ops.exception.DeletionNotAllowedException;
import com.merchant.ops.mapper.DishFlavorMapper;
import com.merchant.ops.mapper.DishMapper;
import com.merchant.ops.mapper.SetmealDishMapper;
import com.merchant.ops.result.PageResult;
import com.merchant.ops.result.Result;
import com.merchant.ops.service.DishService;
import com.merchant.ops.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;


    /*
     * 菜品查询
     * @param dish
     * @return
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);
        List<DishVO> dishVOList = new ArrayList<>();
        for (Dish d : dishList){
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);
            //根据菜品id查询口味数据
            List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(d.getId());
            dishVO.setFlavors(dishFlavors);
            dishVOList.add(dishVO);

        }
        return dishVOList;
    }

    /*
     * 新增菜品与对应口味
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
       //向菜品表插入1条数据
        dishMapper.insert(dish);
        //获取插入后的id
        Long dishId = dish.getId();

       List<DishFlavor> flavors = dishDTO.getFlavors();
       if(flavors != null && flavors.size() > 0){
           flavors.forEach(dishFlavor -> {
               dishFlavor.setDishId(dishId);
           });
               //向口味表插入n条数据
               dishFlavorMapper.insertBatch(flavors);
           }
       }

       /*
     * 菜品查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }
    /*
     * 批量删除
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断菜品是否停售
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //是否被套餐关联
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);        //删除菜品表里的数据
        if(setmealIds!=null && setmealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
   /*     //删除菜品表里的数据
        for (Long id : ids) {
            dishMapper.deleteById(id);
        }*/

        dishMapper.deleteByIds(ids);
/*        //删除菜品关联的口味数据
        for (Long id : ids) {
            dishFlavorMapper.deleteByDishId(id);
        }*/
        dishFlavorMapper.deleteByDishIds(ids);
    }


    /*
     *根据id查询菜品和对应的口味
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        //根据菜品id查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //组装VO数据并返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }


    /*
     * 根据id修改菜品基本信息和对应口味信息
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //修改菜品表的基本信息
        dishMapper.update(dish);
        //删除对应的口味信息
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //重新插入口味信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            //重新插入n条口味数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }



    /*
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<DishVO> list(Long categoryId) {
        //限定只查这个分类下的菜品
        //只查【上架、起售】的菜品，下架菜品不查
        Dish dish = Dish.builder()
                .status(StatusConstant.ENABLE)
                .categoryId(categoryId)
                .build();
        return listWithFlavor(dish);
    }


    /*
     * 批量起售停售
     * @param status
     * @param ids
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
    }

}



