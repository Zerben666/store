package com.cy.store.mappper;

// 使用JUnit 4测试

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cy.store.entity.User;
import com.cy.store.mapper.UserMapper;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserMapperTests {
    /**
     * 单元测试方法：可以独立运行，不用启动整个顶目，提升了代码的测试效率
     * 1.必须被@Test注解修饰
     * 2.返回值类型必须是void
     * 3.方法的参数列表不指定任何类型
     * 4.方法的访问修饰符必须是public
     */

    @Autowired
    private UserMapper userMapper;
    @Test
    public void insert(){
        User user = new User();
        user.setUsername("Tim");
        user.setPassword("123");
        Integer rows = userMapper.insert(user);
        System.out.println(rows);
    }

    @Test
    public void findByUsername() {
        User user = userMapper.findByUsername("Tim");
        System.out.println(user);
    }

    @Test
    public void updatePasswordByUid(){
        userMapper.updatePasswordByUid(
                14,"3211",
                "管理员",new Date() );
    };
    @Test
    public void findByUid(){
        System.out.println(userMapper.findByUid(14));
    };


    @Test
    public void updateInfoByUid(){
        User user = new User();
        user.setUid(20);
        user.setPhone("15511110000");
        user.setEmail("test002@qq.com");
        user.setGender(1);
        userMapper.updateInfoByUid(user);
    }

    @Test
    public void updateAvatarByUid(){
        userMapper.updateAvatarByUid(
                21,"/upload/avatar.png",
                "管理员",new Date() );
    }
}
