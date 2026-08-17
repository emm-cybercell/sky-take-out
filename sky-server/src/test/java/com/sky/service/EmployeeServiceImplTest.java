package com.sky.service;

import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 员工登录逻辑单元测试：覆盖 BCrypt 校验、历史 MD5 迁移、异常分支
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private com.sky.service.impl.EmployeeServiceImpl employeeService;

    private EmployeeLoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        loginDTO = new EmployeeLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("123456");
    }

    private Employee bcryptEmployee() {
        return Employee.builder()
                .id(1L)
                .username("admin")
                .password("$2a$10$X9aDKJdwACXjJ5mi./psn.8DGRiP1MM8tdfRq4Gfiqz5x.lvMWh9e") // 123456 的 BCrypt
                .status(StatusConstant.ENABLE)
                .build();
    }

    @Test
    @DisplayName("登录成功：BCrypt 密码匹配且账号启用")
    void loginSuccess() {
        Employee employee = bcryptEmployee();
        when(employeeMapper.getByUsername("admin")).thenReturn(employee);
        when(passwordEncoder.matches("123456", employee.getPassword())).thenReturn(true);

        Employee result = employeeService.login(loginDTO);

        assertEquals(1L, result.getId());
        // BCrypt 路径不应触发 MD5 升级
        verify(employeeMapper, never()).update(any());
    }

    @Test
    @DisplayName("账号不存在抛异常")
    void loginAccountNotFound() {
        when(employeeMapper.getByUsername("admin")).thenReturn(null);
        assertThrows(AccountNotFoundException.class, () -> employeeService.login(loginDTO));
    }

    @Test
    @DisplayName("密码错误抛异常")
    void loginWrongPassword() {
        Employee employee = bcryptEmployee();
        when(employeeMapper.getByUsername("admin")).thenReturn(employee);
        when(passwordEncoder.matches("123456", employee.getPassword())).thenReturn(false);

        assertThrows(PasswordErrorException.class, () -> employeeService.login(loginDTO));
    }

    @Test
    @DisplayName("账号被锁定抛异常")
    void loginLocked() {
        Employee employee = Employee.builder()
                .id(1L).username("admin")
                .password("$2a$10$X9aDKJdwACXjJ5mi./psn.8DGRiP1MM8tdfRq4Gfiqz5x.lvMWh9e")
                .status(StatusConstant.DISABLE)
                .build();
        when(employeeMapper.getByUsername("admin")).thenReturn(employee);
        when(passwordEncoder.matches("123456", employee.getPassword())).thenReturn(true);

        assertThrows(AccountLockedException.class, () -> employeeService.login(loginDTO));
    }

    @Test
    @DisplayName("历史 MD5 密码：校验通过后自动升级为 BCrypt")
    void loginMigratesLegacyMd5Password() {
        // 模拟旧账号：数据库中密码仍为 MD5(123456)
        Employee employee = Employee.builder()
                .id(1L)
                .username("admin")
                .password(DigestUtils.md5DigestAsHex("123456".getBytes()))
                .status(StatusConstant.ENABLE)
                .build();
        when(employeeMapper.getByUsername("admin")).thenReturn(employee);
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$migrated.hash.xxx");

        assertDoesNotThrow(() -> employeeService.login(loginDTO));
        // 触发一次性地密码升级更新
        verify(employeeMapper).update(any(Employee.class));
    }

    @Test
    @DisplayName("历史 MD5 密码错误：不触发升级且抛异常")
    void loginLegacyMd5WrongPassword() {
        Employee employee = Employee.builder()
                .id(1L)
                .username("admin")
                .password(DigestUtils.md5DigestAsHex("wrong-password".getBytes()))
                .status(StatusConstant.ENABLE)
                .build();
        when(employeeMapper.getByUsername("admin")).thenReturn(employee);

        assertThrows(PasswordErrorException.class, () -> employeeService.login(loginDTO));
        verify(employeeMapper, never()).update(any());
    }
}