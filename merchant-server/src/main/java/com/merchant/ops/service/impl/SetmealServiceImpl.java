package com.merchant.ops.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.merchant.ops.constant.MessageConstant;
import com.merchant.ops.constant.StatusConstant;
import com.merchant.ops.dto.DishPageQueryDTO;
import com.merchant.ops.dto.SetmealDTO;
import com.merchant.ops.dto.SetmealPageQueryDTO;
import com.merchant.ops.entity.Dish;
import com.merchant.ops.entity.DishFlavor;
import com.merchant.ops.entity.Setmeal;
import com.merchant.ops.entity.SetmealDish;
import com.merchant.ops.exception.DeletionNotAllowedException;
import com.merchant.ops.mapper.SetmealDishMapper;
import com.merchant.ops.mapper.SetmealMapper;
import com.merchant.ops.result.PageResult;
import com.merchant.ops.result.Result;
import com.merchant.ops.service.SetmealService;
import com.merchant.ops.vo.DishItemVO;
import com.merchant.ops.vo.DishVO;
import com.merchant.ops.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /*
     * 新增套餐，同时增加套餐和菜品关系
     * @param setmealDTO
     */
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        //新建一个套餐对象
        Setmeal setmeal = new Setmeal();
        //将DTO对象拷贝到套餐对象
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //将套餐对象插入到套餐表
        setmealMapper.insert(setmeal);
        //获取套餐的id
        Long setmealId = setmeal.getId();
        //获取套餐的菜品集合
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //给菜品集合添加套餐id
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        ////保存套餐和菜品的关系
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /*
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     *
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }



    /*
     * 批量删除
     * @param ids
     * @return
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否在起售中
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                //起售中的菜品不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //删除套餐表中的数据
        setmealMapper.deleteBatch(ids);
        //删除套餐和菜品关系表中的数据
        setmealDishMapper.deleteBySetmealIds(ids);

    }
/*
*根据ids查询套餐信息
* @param ids
* @return
 */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        //1. 查套餐主表 单个数据
        Setmeal setmeal = setmealMapper.getById(id);
        //2. 根据套餐id，查中间表 所有关联菜品
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        //3. 组装到一个VO里返回
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }


    /*
    * 批量起售停售
    * @param status
    * @param ids
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(status == StatusConstant.ENABLE){
            List<Dish> dishList = setmealDishMapper.getDishBySetmealId(id);
            for (Dish dish : dishList) {
                if(dish.getStatus() == StatusConstant.DISABLE){
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
                setmealMapper.update(setmeal);
    }

    /*
    *查询套餐
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;

    }

    @Override
    public Result<List<SetmealDish>> getById(Long id) {
        return null;
    }

    /*
    *修改套餐
    * @param setmealDTO
    * @return
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //修改套餐表
        setmealMapper.update(setmeal);

        //删除套餐和菜品关系表中的数据
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        //插入套餐表数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
        });
        //重新插入套餐信息
        setmealDishMapper.insertBatch(setmealDishes);
    }



    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}



