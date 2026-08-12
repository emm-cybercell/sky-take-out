package com.sky.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询订单详情
     * 
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 各个状态的订单数量统计
     * 
     * @return
     */
    @Select("select count(*) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 查询订单状态和下单时间
     * 
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime orderTime);

    /**
     * 根据条件查询订单总额
     * 
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据动态条件统计订单数量
     * 
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 统计指定时间区间内销量排名top10
     * 
     * @param begin
     * @param end
     * @return
     */
    
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
