package com.cy.store.mappper;

// 使用JUnit 4测试

import com.cy.store.entity.Order;
import com.cy.store.entity.OrderItem;
import com.cy.store.mapper.OrderMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OrderMapperTests {

    @Autowired
    private OrderMapper orderMapper;
    @Test
    public void insertOrder() {
        Order order = new Order();
        order.setUid(22);
        order.setRecvName("明明");
        order.setRecvPhone("17857704444");
        orderMapper.insertOrder(order);
    }
    @Test
    public void insertOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setOid(1);
        orderItem.setPid(10000003);
        orderItem.setTitle("AAAA");
        orderMapper.insertOrderItem(orderItem);
    }
}
