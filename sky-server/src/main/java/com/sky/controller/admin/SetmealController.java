package com.sky.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 套餐管理接口
 */
@RestController// 表明该类是一个 控制器，处理 HTTP 请求并返回数据（JSON）
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {
    @Autowired//自动创建对象并设置属性
    private SetmealService setmealService;

    /**
     * 新增套餐
     * 
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐接口")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐分页查询
     * 
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询接口")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询：{}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     */
    @DeleteMapping
    @ApiOperation("批量删除接口")
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除：{}",ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }
    
    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐接口")
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改菜品:{}",setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }
    
    /**
     * 套餐起售停售
     */
    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售停售接口")
    public Result startOrStop(@PathVariable("status") Integer status,Long id){
        setmealService.startOrStop(status,id);
        return Result.success();
    }
}
