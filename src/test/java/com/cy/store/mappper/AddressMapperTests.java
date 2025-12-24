package com.cy.store.mappper;

// 使用JUnit 4测试

import com.cy.store.entity.Address;
import com.cy.store.mapper.AddressMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AddressMapperTests {
    /**
     * 单元测试方法：可以独立运行，不用启动整个顶目，提升了代码的测试效率
     * 1.必须被@Test注解修饰
     * 2.返回值类型必须是void
     * 3.方法的参数列表不指定任何类型
     * 4.方法的访问修饰符必须是public
     */

    @Autowired
    private AddressMapper addressMapper;
    @Test
    public void insert() {
        Address address = new Address();
        address.setUid(21);
        address.setPhone("17858802974");
        address.setName("测试");
        address.setAddress("详细地址");
        address.setCreatedUser("admin");
        address.setCreatedTime(new Date());
        addressMapper.insert(address);
    }
    @Test
    public void countByUid(){
        Integer count = addressMapper.countByUid(21);
        System.out.println(count);
    }

    @Test
    public void findByUid(){
        List<Address> list = addressMapper.findByUid(21);
        System.out.println(list);
    }


    // 设置默认收货地址 单元测试
    @Test
    public void findByAid(){
        System.out.println(addressMapper.findByAid(8));;
    }
    @Test
    public void updateNotDefault(){
        addressMapper.updateNotDefault(21);
    }
    @Test
    public void updateDefaultByAid(){
        addressMapper.updateDefaultByAid(8,"test",new Date());
    }

    // 删除默认收货地址 单元测试
    @Test
    public void deleteByAid() {
        addressMapper.deleteByAid(5);
    }
    @Test
    public void findLastModified() {
        System.out.println( addressMapper.findLastModified(21) );
    }
}
