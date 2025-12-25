### 1.项目分析

1.项目功能：登录、注册、热销商品、用户管理（密码、个人信息、头像、收货地址）、购物车（展示、增加、删除）、订单模块。

2.开发顺序：注册、登录、用户管理、购物车、商品、订单模块。

3.某一个模块的开发

- 持久层开发：依据前端页面的设置规划相关的SQL语句，以及进行配置
- 业务层开发：核心功能控制、业务操作以及异常处理
- 控制层开发：接受请求、处理响应
- 前端开发：JS、Query、AJAX等技术连接后台

### 2.项目环境

1.JDK：1.8版本及以上

2.maven：配置到idea，>=3.6.1版本

3.数据库：MariaDB/MySQL，>=5.1版本

4.开发平台：idea开发

### 3.搭建项目

1.项目名称：store，表示商城

2.结构：com.cy.store (cy->company)

`java web` `mybatis` `mysql dirver`

3.资源文件：resources文件夹下（static静态资源、templates模板）

4.单元测试：test.com.cy.store

5.在properties文件中配置数据库的连接源信息

```key=value
spring.datasource.url=jdbc:mysql://localhost:3306/store?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456
```

6.创建一个store数据库

```sql
create database store character set utf8;
```

7.测试连接

- 启动SpringBoot主类，是否有对应的Spring图形输出
- 在单元测试中测试数据库的连接是否可以正常加载

8.访问项目静态资源，是否可以正常加载。所有静态资源复制到static目录下

> 注意：idea对于JS代码的兼容性较差，编写了js但有时不能正常加载
> 
> 1.idea缓存清理
> 
> 2.生命周期clean-install
> 
> 3.rebuild重新构建
> 
> 4.重启idea和操作系统