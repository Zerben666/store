package com.cy.store;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.SQLException;

// @SpringBootTest:标注当前的类是一个测试类，不会随同顶目一块打包
@SpringBootTest
class StoreApplicationTests {
    @Autowired //自动装配
    private DataSource dataSource;

    @Test
    void contextLoads() {
    }

    /**
     * 数据库连接池：
     * 1.DBCP
     * 2.C3P0
     * 3.Hikari(Spring Boot默认整合，基于C3P0管理数据库连接对象)
     * HikariProxyConnection@630359980 wrapping com.mysql.cj.jdbc.ConnectionImpl@1abcd059
     */
    @Test
    void getConnection() throws SQLException {
        System.out.println(dataSource.getConnection());
    }
}
