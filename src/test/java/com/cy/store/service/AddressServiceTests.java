package com.cy.store.service;

// 使用JUnit 4测试

import com.cy.store.entity.Address;
import com.cy.store.service.ex.ServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AddressServiceTests {
    /**
     * 单元测试方法：可以独立运行，不用启动整个顶目，提升了代码的测试效率
     * 1.必须被@Test注解修饰
     * 2.返回值类型必须是void
     * 3.方法的参数列表不指定任何类型
     * 4.方法的访问修饰符必须是public
     */

    @Autowired
    private IAddressService addressService;
    @Test
    public void addNewAddress(){
        Address address = new Address();
        address.setUid(21);
        address.setPhone("17858809999");
        address.setName("测试2");
        addressService.addNewAddress( 21,"管理员",address );
    }

    @Test
    public void setDefault(){
        addressService.setDefault( 8,21,"admin" );
    }

    // 删除收货地址 测试
    @Test
    public void delete(){
        addressService.delete( 9,21,"test" );
    }
}
