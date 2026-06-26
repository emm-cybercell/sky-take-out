package com.sky.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;

import lombok.extern.slf4j.Slf4j;

@Service // 表明该类是一个服务类，处理业务逻辑
@Slf4j
public class SetmealServicempl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐，保存套餐和菜品的关联关系
     * 
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 向套餐表插入数据
        setmealMapper.insert(setmeal);
        // 获取生成的套餐id
        Long setmealId = setmeal.getId();

        // 向套餐表插入n个菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐分页查询
     * 
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        //MyBatis 分页插件设置分页: 第 page 页，每页 pageSize 条
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除套餐
     * 
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids){
        //判断套餐是否在售
        for(Long id : ids){
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //根据id批量删除套餐数据和菜品口味数据
        setmealMapper.deleteBatch(ids);
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    /**
     * 修改套餐
     * 
     * @param setmealDTO
     * @return
     */
    public void update(SetmealDTO setmealDTO){
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //修改套餐表
        setmealMapper.update(setmeal);
        //删除套餐和菜品的关联关系
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        //重新插入新的套餐和菜品的关联关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            });
            //向套餐表插入n条数据
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐起售停售
     * 
     * @param status
     * @param id
     */
    public void startOrStop(Integer status ,Long id){
        if(status == StatusConstant.DISABLE){
            List<Dish> dishes = setmealDishMapper.getBySetmealId(id);
            if(dishes != null && dishes.size() > 0){
                dishes.forEach(dish -> {
                    if(dish.getStatus() == StatusConstant.DISABLE){
                        throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        setmealMapper.update(setmeal);
    }
}
