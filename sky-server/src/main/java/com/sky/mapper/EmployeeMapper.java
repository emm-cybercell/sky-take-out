//操作数据库
package com.sky.mapper;

import com.sky.entity.Employee;

import com.sky.dto.EmployeePageQueryDTO;
import com.sky.result.PageResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.sky.annotation.AutoFill;
import com.sky.enumeration.OperationType;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * 
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 新增员工
     * 
     * @param employee
     */
    @Insert("insert into employee(username, name, phone, sex, id_number, password, status, create_time, update_time, create_user, update_user) "
            +
            "values(#{username}, #{name}, #{phone}, #{sex}, #{idNumber}, #{password}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Employee employee);

    /**
     * 员工分页查询
     * 
     * @param employeePageQueryDTO
     * @return
     */

    com.github.pagehelper.Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);

    /**
     * 根据 id 查询员工信息
     * @param id
     * @return
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id); 
}
