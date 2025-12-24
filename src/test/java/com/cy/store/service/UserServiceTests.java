package com.cy.store.service;

// 使用JUnit 4测试

import com.cy.store.entity.User;
import com.cy.store.mapper.UserMapper;
import com.cy.store.service.ex.ServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTests {
    /**
     * 单元测试方法：可以独立运行，不用启动整个顶目，提升了代码的测试效率
     * 1.必须被@Test注解修饰
     * 2.返回值类型必须是void
     * 3.方法的参数列表不指定任何类型
     * 4.方法的访问修饰符必须是public
     */

    @Autowired
    private IUserService userService;
    @Test
    public void reg(){
        try {
            User user = new User();
            user.setUsername("md5_test3");
            user.setPassword("123");
            userService.reg(user);
            System.out.println("OK");
        } catch (ServiceException e) {
            // 获取类的对象，再获取类的名称
            System.out.println( e.getClass().getSimpleName() );
            // 打印异常描述信息
            System.out.println( e.getMessage() );
        }
    }

    // 6.2.2单元测试 测试业务层登录的方法是否可以执行通过。记得给所有字段赋值，看是否只返回uid，username和头像（avatar）
    @Test
    public void login(){
        User user = userService.login("test01","123");
        System.out.println(user);
    }

    @Test
    public void changePassword(){
        userService.changePassword( 20,"管理员","123","321");
    }

    @Test
    public void getByUid(){
        System.err.println( userService.getByUid(20) );
    }
    @Test
    public void changeInfo(){
        User user = new User();
        user.setPhone("17844440000");
        user.setEmail("yuan@qq.com");
        user.setGender(0);
        userService.changeInfo( 20,"管理员2",user );
    }

    @Test
    public void changeAvatar(){
        userService.changeAvatar( 21,"/upload/test.png","小明" );
    }
}
