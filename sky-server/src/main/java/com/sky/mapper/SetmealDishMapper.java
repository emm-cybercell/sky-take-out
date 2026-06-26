package com.sky.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询对应的套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量菜品数据
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 批量删除套餐菜品数据
     * @param ids
     */
    void deleteBySetmealIds(List<Long> ids);

    /**
     * 根据套餐id删除菜品数据
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

    /**
     * 根据套餐id查询菜品数据
     * @param setmealId
     * @return 
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);
}
