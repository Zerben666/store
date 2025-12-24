package com.cy.store.service;

// 使用JUnit 4测试

import com.cy.store.entity.District;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DistrictServiceTests {
    /**
     * 单元测试方法：可以独立运行，不用启动整个顶目，提升了代码的测试效率
     * 1.必须被@Test注解修饰
     * 2.返回值类型必须是void
     * 3.方法的参数列表不指定任何类型
     * 4.方法的访问修饰符必须是public
     */

    @Autowired
    private IDistrictService districtService;
    @Test
    public void getByParent(){
        // 86表示中国,所有省的父代号都是86
        List<District> list = districtService.getByParent("86");
        for(District d : list){
            System.err.println(d);
        }
    }
}
