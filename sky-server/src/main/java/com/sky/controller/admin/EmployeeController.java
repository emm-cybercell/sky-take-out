//接受请求
package com.sky.controller.admin;

import com.sky.result.PageResult;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.dto.EmployeeDTO;
import com.sky.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工管理相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录接口")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        // 登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();// 创建一个空的 Claims 集合。Claims 是 JWT 中携带的"声明"（即用户信息负载）
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());// 将当前登录员工的 ID 放入 claims 中，后续可以从 token 中解析
        // 调用 JWT 工具类创建令牌
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(), // 管理端 JWT 的签名密钥，用于加密和验证 token 合法性
                jwtProperties.getAdminTtl(), // token 的有效期，过期需要重新登录
                claims);
        // Lombok 的 @Builder：创建对象的构建器模式，简化对象创建过程，提升代码可读性和维护性
        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);// 将登录结果封装在 Result 对象中返回给前端，包含员工信息和 JWT 令牌
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation(value = "员工退出接口")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     * 
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增员工接口")
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        // 数据是放在 HTTP 请求的 body 里的 JSON 字符串，
        // Spring 用 @RequestBody 把这个 JSON 反序列化成EmployeeDTO 对象
        log.info("新增员工：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 员工分页查询
     * 
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "员工分页查询接口")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        // 当参数是一个对象（如 EmployeePageQueryDTO）且没有注解时，
        // 它会自动把请求中的查询参数按字段名绑定到这个对象的属性上
        log.info("员工分页查询：{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用禁用员工账号
     * 
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation(value = "启用禁用员工接口")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        // @PathVariable 提取的是 URL 路径中的占位符部分
        log.info("启用禁用员工账号：{},{}", status, id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据 id 查询员工信息
     * 
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据 id 查询员工信息接口")
    public Result<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    } 

    /**
     * 修改员工信息
     * 
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation(value = "修改员工信息接口")
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        log.info("编辑员工信息：{}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }
    /**
     * 修改密码
     * @param passwordeditDTO
     * @return
     */
    @PutMapping("/editPassword")
    @ApiOperation(value = "修改密码接口")
    public Result editPassword(@RequestBody PasswordEditDTO passwordeditDTO){
        log.info("修改密码:{}", passwordeditDTO);
        employeeService.editPassword(passwordeditDTO);
        return Result.success();
    }
}