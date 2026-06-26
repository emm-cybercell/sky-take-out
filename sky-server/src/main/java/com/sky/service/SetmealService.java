package com.sky.service;

import java.util.List;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

public interface SetmealService {
    /**
     * 新增套餐
     * @param setmealDTO
     */
    public void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 批量删除套餐
     * @param ids
     */
    public void deleteBatch(List<Long> ids);

    /**
     * 修改套餐
     * 
     * @param setmealDTO
     * @return
     */
    public void update(SetmealDTO setmealDTO);

    /**
     * 套餐起售停售
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id);
}
