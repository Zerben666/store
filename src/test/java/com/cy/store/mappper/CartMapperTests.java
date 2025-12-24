package com.cy.store.mappper;

// 使用JUnit 4测试

import com.cy.store.entity.Cart;
import com.cy.store.mapper.CartMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CartMapperTests {
    /**
     * 单元测试方法：可以独立运行，不用启动整个顶目，提升了代码的测试效率
     * 1.必须被@Test注解修饰
     * 2.返回值类型必须是void
     * 3.方法的参数列表不指定任何类型
     * 4.方法的访问修饰符必须是public
     */

    @Autowired
    private CartMapper cartMapper;

    @Test
    public void insert() {
        Cart cart = new Cart();
        cart.setUid(22);
        cart.setPid(10000011);
        cart.setNum(2);
        cart.setPrice(1000L);
        cartMapper.insert(cart);
    }
    @Test
    public void updateNumByCid(){
        cartMapper.updateNumByCid( 1,4,"张三",new Date() );
    }
    @Test
    public void findByUidAndPid(){
        Cart cart = cartMapper.findByUidAndPid( 22,10000011 );
        System.err.println(cart);
    }

    @Test
    public void findOVByUid(){
        System.out.println( cartMapper.findVOByUid(21) );
    }

    @Test
    public void findByCid(){
        System.out.println(cartMapper.findByCid(1));
    }

    @Test
    public void findVOByCid(){
        Integer[] cids = {1,3,5,7};
        System.out.println(cartMapper.findVOByCid(cids));
    }
}
