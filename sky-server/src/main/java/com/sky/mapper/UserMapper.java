package com.sky.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.sky.entity.User;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @Select("select * from user where id = #{id}")
    User getById(Long id);

    /**
     * 根据条件查询用户数量
     * @param map
     */
    Integer countByMap(Map map);
}
