package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
@RequiredArgsConstructor
public class DishController {
    private final DishService dishService;

    /**
     * 根据分类id查询菜品
     *
     * 缓存规则：同一分类的菜品列表缓存 30 分钟（key: dishCache::分类id），
     * 后台新增/修改/删除/启售停售菜品时通过 @CacheEvict 清空对应缓存
     *
     * @param categoryId
     * @return
     */
    @Cacheable(cacheNames = "dishCache", key = "#categoryId")
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        //查询数据库
        List<DishVO> list = dishService.listWithFlavor(dish);

        return Result.success(list);
    }

}
