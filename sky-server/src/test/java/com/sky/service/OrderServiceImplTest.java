package com.sky.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.impl.OrderServiceImpl;
import com.sky.utils.RedisLock;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderSubmitVO;
import com.sky.websocket.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户下单逻辑单元测试：覆盖防重复提交（分布式锁）、主流程、异常分支
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderDetailMapper orderDetailMapper;
    @Mock
    private AddressBookMapper addressBookMapper;
    @Mock
    private ShoppingCartMapper shoppingCartMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private WeChatPayUtil weChatPayUtil;
    @Mock
    private WebSocketServer webSocketServer;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RedisLock redisLock;

    @InjectMocks
    private OrderServiceImpl orderService;

    private static final Long USER_ID = 1001L;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(USER_ID);
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    private OrdersSubmitDTO buildSubmitDTO() {
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(10L);
        dto.setPayMethod(1);
        dto.setAmount(new BigDecimal("66.6"));
        return dto;
    }

    private AddressBook buildAddressBook() {
        return AddressBook.builder()
                .id(10L)
                .consignee("张三")
                .phone("13800138000")
                .detail("北京市海淀区上地十街10号")
                .build();
    }

    private List<ShoppingCart> buildCart() {
        ShoppingCart cart = new ShoppingCart();
        cart.setName("宫保鸡丁");
        cart.setDishId(5L);
        cart.setNumber(1);
        cart.setAmount(new BigDecimal("38.0"));
        return Collections.singletonList(cart);
    }

    @Test
    @DisplayName("重复提交：锁被占用时直接抛业务异常且不落库")
    void submitWhenLockOccupied() {
        OrdersSubmitDTO dto = buildSubmitDTO();
        when(redisLock.lock("order:submit:user:" + USER_ID, 5)).thenReturn(null);

        OrderBusinessException ex = assertThrows(OrderBusinessException.class, () -> orderService.submitOrder(dto));
        assertEquals("订单正在提交中，请勿重复操作", ex.getMessage());
        // 锁未获取：不查地址、不写订单
        verify(orderMapper, never()).insert(any());
        verify(addressBookMapper, never()).getById(any());
        verify(redisLock, never()).unlock(any(), any());
    }

    @Test
    @DisplayName("下单成功：雪花订单号 + 落库 + 清空购物车 + 释放锁")
    void submitOrderSuccess() {
        OrdersSubmitDTO dto = buildSubmitDTO();
        when(redisLock.lock("order:submit:user:" + USER_ID, 5)).thenReturn("lock-uuid");
        when(addressBookMapper.getById(10L)).thenReturn(buildAddressBook());
        when(shoppingCartMapper.list(any())).thenReturn(buildCart());
        // 模拟 MyBatis 回填订单主键
        org.mockito.Mockito.doAnswer(inv -> {
            Orders o = inv.getArgument(0);
            o.setId(999L);
            return null;
        }).when(orderMapper).insert(any(Orders.class));

        OrderSubmitVO vo = orderService.submitOrder(dto);

        assertNotNull(vo);
        assertEquals(999L, vo.getId());
        assertEquals(new BigDecimal("66.6"), vo.getOrderAmount());

        // 订单号必须是雪花算法生成的 17~19 位数字（而非原时间戳 13 位）
        String orderNumber = vo.getOrderNumber();
        assertTrue(orderNumber.matches("\\d{17,19}"), "订单号异常：" + orderNumber);

        // 订单落库 + 明细批量插入 + 购物车清空
        ArgumentCaptor<Orders> ordersCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(orderMapper).insert(ordersCaptor.capture());
        assertEquals(USER_ID, ordersCaptor.getValue().getUserId());
        assertEquals(Orders.PENDING_PAYMENT, ordersCaptor.getValue().getStatus());
        assertEquals(Orders.UN_PAID, ordersCaptor.getValue().getPayStatus());
        assertEquals("张三", ordersCaptor.getValue().getConsignee());

        verify(orderDetailMapper).insertBatch(any(List.class));
        verify(shoppingCartMapper).deleteByUserId(USER_ID);
        // 业务结束后必须释放锁
        verify(redisLock).unlock("order:submit:user:" + USER_ID, "lock-uuid");
    }

    @Test
    @DisplayName("地址簿为空时下单失败，锁正常释放")
    void submitWhenAddressBookNull() {
        OrdersSubmitDTO dto = buildSubmitDTO();
        when(redisLock.lock("order:submit:user:" + USER_ID, 5)).thenReturn("lock-uuid");
        when(addressBookMapper.getById(10L)).thenReturn(null);

        assertThrows(AddressBookBusinessException.class, () -> orderService.submitOrder(dto));
        verify(redisLock).unlock(eq("order:submit:user:" + USER_ID), eq("lock-uuid"));
    }

    @Test
    @DisplayName("购物车为空时下单失败，锁正常释放")
    void submitWhenCartEmpty() {
        OrdersSubmitDTO dto = buildSubmitDTO();
        when(redisLock.lock("order:submit:user:" + USER_ID, 5)).thenReturn("lock-uuid");
        when(addressBookMapper.getById(10L)).thenReturn(buildAddressBook());
        when(shoppingCartMapper.list(any())).thenReturn(Collections.emptyList());

        assertThrows(ShoppingCartBusinessException.class, () -> orderService.submitOrder(dto));
        verify(redisLock).unlock(eq("order:submit:user:" + USER_ID), eq("lock-uuid"));
        verify(orderMapper, never()).insert(any());
    }
}