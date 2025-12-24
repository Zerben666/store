package com.cy.store.service;

// 使用JUnit 4测试

import com.cy.store.entity.Address;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CartServiceTest {
    /**
     * 单元测试方法：可以独立运行，不用启动整个顶目，提升了代码的测试效率
     * 1.必须被@Test注解修饰
     * 2.返回值类型必须是void
     * 3.方法的参数列表不指定任何类型
     * 4.方法的访问修饰符必须是public
     */

    @Autowired
    private ICartService cartService;
    /*@Test
    public void addToCard() {
        cartService.addToCArt( 21,10000003,5,"test" );
    }*/
    /*// 执行第一条后在执行相当于在存在的基础上更新
    @Test
    public void addToCard() {
        cartService.addToCArt( 21,10000003,5+2,"test" );
    }*/
    // 添加新的商品
    @Test
    public void addToCard() {
        cartService.addToCArt( 21,10000011,2,"test" );
    }
}
