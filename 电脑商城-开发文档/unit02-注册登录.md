## 用户注册

### 1 创建数据表

1.选中数据表

```mysql
use store;
```

2.创建t_user表

```mysql
CREATE TABLE t_user (
    uid INT AUTO_INCREMENT COMMENT '用户id',
    username VARCHAR(20) NOT NULL UNIQUE COMMENT '用户名',
    password CHAR(32) NOT NULL COMMENT '密码',
    salt CHAR(36) COMMENT '盐值',
    phone VARCHAR(20) COMMENT '电话号码',
    email VARCHAR(30) COMMENT '电子邮箱',
    gender INT COMMENT '性别:0-女，1-男',
    avatar VARCHAR(50) COMMENT '头像',
    is_delete INT COMMENT '是否删除：0-未删除，1-已删除',
    created_user VARCHAR(20) COMMENT '日志-创建人',
    created_time DATETIME COMMENT '日志-创建时间',
    modified_user VARCHAR(20) COMMENT '日志-最后修改执行人',
    modified_time DATETIME COMMENT '日志-最后修改时间',
    PRIMARY KEY (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

<br/>

### 2 创建用户的实体类

1.通过表的结构提取出表的公共字段，放在一个实体类的基类中，起名BaseEntity。

```
src/main/java/com.cy.store/entity/BaseEntity.java
```

2.创建用户的实体类。需要继承BaseEntity基类

```
[..]com.cy.store.entity/User.java
```

### 

### 3 注册-持久层

通过MyBaties来操作数据库

#### 3.1规划需要执行的SQL语句

1.用户的注册功能，数据的插入操作

```mysql
insert into t_user (username,password) values (值列表);
```

2.在用户注册时首先查询当前用户名是否存在，如果存在则不能注册。

```mysql
select * from t_user where username=?;
```

#### 3.2设计接口和抽象方法

1.定义Mapper接口。在项目的目录结构下首先创建一个mapper包。在这个包下再根据不同的功能模块来创建mapper接口。创建一个UserMapper的接口。要在接口中定义这两个SQL语句抽象方法。

`com.cy.store.entity.mapper.UserMapper`

```java
package com.cy.store.entity.mapper;

/** 用户模块的持久层接口 */
//@mapper
public interface UserMapper {
    /**
     * 插入用户的数据
     * @param user 用户的数据
     * @return 受影响的行数（增、删、改 ，受影响的行数作为返回值，以此判断是否执行成功）
     */
    Integer insert(User user);

    /**
     * 根据用户名来查询用户的数据
     * @param username 用户名
     * @return 如果找到对应的用户则返回其数据，如果没有找到则返回null值
     */
    User findByUsername(String username);
}
```

2.在启动类配置mapper接口文件的位置

```java
// MapperScan注解：指定当前项目中的Mapper接口路径的位置,项目启动时自动加载接口文件
@MapperScan("com.cy.store.mapper")
```

<br/>

#### 3.3编写映射

1.定义xml映射文件，与对应的接口进行关联。所有的映射文件需要放置resources目录下。在此目录创建mapper文件夹，存放Mapper映射文件

2.创建接口对应的映射文件，遵循和接口名称保持一致即可。创建一个UserMapper.xml文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!--namespace属性：用于指定当前的映射文件和哪个接口进行映射，需要指定接口的文件路径，需要标注包的完整路径接口-->
<mapper namespace="com.cy.store.mapper.UserMapper">
    
</mapper>
```

3.配置接口中的方法对应上SQL语句，需要借助标签来完成：insert/update/delete/select，对应SQL语句增删改查。

```java
<!--自定义映射规则：resultMap标签来完成映射规则的定义-->
    <resultMap id="UserEntityMap" type="com.cy.store.entity.User">
        <!--配合完成名称不一致的映射(主键不能省略)-->
        <id column="uid" property="uid"></id>
        <result column="is_delete" property="isDelete"></result>
        <result column="create_user" property="createUser"></result>
        <result column="create_time" property="createTime"></result>
        <result column="modified_user" property="modifiedUser"></result>
        <result column="modified_time" property="modifiedTime"></result>
    </resultMap>
    
    <!-- id属性：表示映射的接口中方法的名称，直接在标签的内部来编写SQL语句-->
    <insert id="insert" useGeneratedKeys="true" keyProperty="uid">
        INSERT INTO t_user (
            username, password, salt,
            phone, email, gender, avatar,
            is_delete, created_user, created_time, modified_user, modified_time
        ) VALUES (
            #{username}, #{password}, #{salt},
            #{phone}, #{email}, #{gender}, #{avatar},
            #{isDelete}, #{createdUser}, #{createdTime}, #{modifiedUser}, #{modifiedTime}
        )
    </insert>

    <select id="findByUsername" resultMap="UserEntityMap">
        SELECT * FROM t_user WHERE username = #{username}
    </select>
```

4.[检查]将mapper文件的位置注册到properties对应的配置文件中。

```properties
mybatis.mapper-locations = classpath:mapper/*xml
```

4.单元测试：每个独立的层编写完毕后使用单元测试方法测试当前功能

- 在test包结构下创建mapper包，在此创建持久层的功能测试

```java
package com.cy.store.mappper;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cy.store.entity.User;
import com.cy.store.mapper.UserMapper;
import org.springframework.test.context.junit4.SpringRunner;

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
        //userMapper.insert(user);
    }

    @Test
    public void findByUsername() {
        User user = userMapper.findByUsername("Tim");
        System.out.println(user);
    }
}

```

<br/>

### 4 注册-业务层

#### 4.1规划异常

0.RuntimeException异常，作为异常的子类，然后再去定义具体的异常类型来继承这个异常。创建业务层异常的基类，ServiceException异常，统一让它继承RuntimeException异常。异常机制建立。

```java
package com.cy.store.service.ex;

/** 业务层异常的基类：throws new ServiceException("业务层产生未知的异常") */
public class ServiceException extends RuntimeException{
    public ServiceException() {
        super();
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    protected ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

```

> 根据业务层不同的功能来详细定义具体的异常类型，统一继承ServiceException异常类

1.用户注册可能产生用户名被占用，抛出一个异常：`UsernameDuplicatedException`异常

```java
package com.cy.store.service.ex;

/** 用户名被占用的异常 */
public class UsernameDuplicatedException extends ServiceException{
    public UsernameDuplicatedException() {
        super();
    }

    public UsernameDuplicatedException(String message) {
        super(message);
    }

    public UsernameDuplicatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsernameDuplicatedException(Throwable cause) {
        super(cause);
    }

    protected UsernameDuplicatedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

```

2.正在执行数据插入操作的时候，服务器/数据库宕机。处于正在执行插入的过程中所产生的异常`InsertException`

```java
package com.cy.store.service.ex;

/** 数据在插入过程中产生的异常 */
public class InsertException extends ServiceException{
    public InsertException() {
        super();
    }

    public InsertException(String message) {
        super(message);
    }

    public InsertException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsertException(Throwable cause) {
        super(cause);
    }

    protected InsertException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

```

#### 4.2 设计接口和抽象方法

在service包下创建一个`IUserService`接口。

```java
package com.cy.store.service;

import com.cy.store.entity.User;

/** 用户模块业务层接口 */
public interface IUserService {
    /**
     * 用户注册方法
     * @param user 用户的数据对象
     */
    void reg(User user);
}
```

2.创建一个实现类`UserServiceImpl`类，需要实现这个接口，并且实现抽象方法

```java
package com.cy.store.service.impl;

import com.cy.store.entity.User;
import com.cy.store.mapper.UserMapper;
import com.cy.store.service.IUserService;
import com.cy.store.service.ex.InsertException;
import com.cy.store.service.ex.UsernameDuplicatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.UUID;

/** 用户模块业务层的实现类 */
@Service // @Service注解：将当前类的对象交给Spring来管理，自动创建对象以及对象的维护
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public void reg(User user) {
        // 通过user参数来获取传递过来的username
        String username = user.getUsername();
        // 调用findByUsername(username)判断用户是否被注册过
        User result = userMapper.findByUsername(username);

        // 结果集不为null则抛出用户名被占用的异常
        if(result != null){
            //抛出异常
            throw new UsernameDuplicatedException("用户名被占用");
        }

        // 密码加密处理的实现：md5 [67dhdsgh-yeuwrey121-yerui374-yrwirei-67123]
        // （串 + password + 串） ----> md5算法加密，连续加载3次
        // 盐值 + password + 盐值 ---- 盐值就是一个随机的字符串
        String oldPassword = user.getPassword();
        //获取全大写盐值（随机生成一个盐值）
        String salt = UUID.randomUUID().toString().toUpperCase();
        // 补全数据：盐值的记录
        user.setSalt(salt);
        // 将密码和盐值最为一个整体进行加密处理
        String md5password = getMD5Password(oldPassword,salt);
        // 将加密之后的密码重新赋给user对象
        user.setPassword(md5password);

        // 补全数据：is_delete设置成0
        user.setIsDelete(0);
        // 补全数据：4个日志字段信息
        user.setCreatedUser( user.getUsername() );  //创建人
        user.setModifiedUser( user.getUsername() ); //最后修改人
        Date date = new Date();
        user.setCreatedTime( date );                //创建时间
        user.setModifiedTime( date );               //最后修改时间

        // 执行注册业务逻辑的实现(rows==1)
        Integer rows = userMapper.insert(user);
        if(rows != 1){
            throw new InsertException("在用户过程中产生了未知的异常");
        }
    }

    /** 定义一个md5算法的加密处理 */
    private String getMD5Password(String password,String salt){
        // md5加密算法调用 ×3
        for (int i =0; i < 3; i++){
            password = DigestUtils.md5DigestAsHex( (salt+password+salt).getBytes() ).toUpperCase();
        }
        return password;
    }
}

```

3.在单元测试包下创建一个`UserServiceTests`类，在这个类中添加单元测试的功能

```java
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
            user.setUsername("md5_test2");
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
}

```

<br/>

### 5 注册-控制层

#### 5.1 创建响应

状态码、状态描述信息、响应数据。这部分功能封装在一个类中，将这个类作为方法返回值，返回给前端浏览器。

```java
/**
 * Json格式的数据进行响应
 */
public class JsonResult<E> implements Serializable {
    /** 状态码 */
    private Integer state;
    /** 描述信息*/
    private String message;
    /** 响应数据*/
    private  E data;
}
```

#### 5.2 设计请求

依据当前的业务功能模块进行请求的设计。

```
请求路径：/users/reg
请求参数：User user
请求类型：POST
响应结果：JsonResult<Void>
```

#### 5.3 处理请求

1.创建一个控制层对应的`UserController`类，依赖于业务层的接口。

```java
package com.cy.store.Controller;

import com.cy.store.entity.User;
import com.cy.store.service.IUserService;
import com.cy.store.service.ex.InsertException;
import com.cy.store.service.ex.UsernameDuplicatedException;
import com.cy.store.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

// @Controller
@RestController // @Controller + @ResponseBody
@RequestMapping("users")
public class UserController {
    @Autowired
    private IUserService userService;

    @RequestMapping("reg")
    // @ResponseBody // 表示此方法的响应结果以json格式进行数据的响应给到前端
    public JsonResult<Void> reg(User user){
        // 创建响应结果对象
        JsonResult<Void> result = new JsonResult<>();
        try {
            userService.reg(user);
            result.setState(200);
            result.setMessage("用户注册成功");
        } catch (UsernameDuplicatedException e) {
            result.setState(4000);
            result.setMessage("用户名被占用");
        } catch (InsertException e) {
            result.setState(5000);
            result.setMessage("注册时产生未知的异常");
        }
        return result;
    }
}
```

测试：`http://localhost:8080/users/reg?username=user0001&password=123456`

```properties
{
  "state": 200,
  "message": "用户注册成功",
  "data": null
}
```

#### 5.4 控制层优化设计

在控制层抽离一个父类，在此统一处理异常相关操作。编写`BaseController`类，统一处理异常。

```java
package com.cy.store.Controller;

import com.cy.store.service.ex.InsertException;
import com.cy.store.service.ex.ServiceException;
import com.cy.store.service.ex.UsernameDuplicatedException;
import com.cy.store.util.JsonResult;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** 表示控制层类的基类*/
public class BaseController {
    /** 操作成功的状态码*/
    public static final int OK = 200;

    // 请求处理方法：返回值需要传递给前端的数据
    // 自动将异常对象传递给此方法的参数列表上
    // 当前项目中产生了异常，被统一拦截到此方法中。这个方法此时就充当请求处理方法，方法的返回值直接给到前端
    @ExceptionHandler(ServiceException.class) // 用于统一处理抛出的异常
    public JsonResult<Void> handleException(Throwable e){
        JsonResult<Void> result = new JsonResult<>(e);
        if(e instanceof UsernameDuplicatedException){
            result.setState(4000);
            result.setMessage("用户名被占用");
        }else if(e instanceof InsertException){
            result.setState(5000);
            result.setMessage("注册时产生未知的异常");
        }
        return  result;
    }
}
```

重新构建`reg`方法

```java
@RequestMapping("reg")
    // @ResponseBody // 表示此方法的响应结果以json格式进行数据的响应给到前端
    public JsonResult<Void> reg(User user){
        userService.reg(user);
        return new JsonResult<>(OK);
    }
```

<br/>

### 6 注册-前端页面

1.在`register`页面中编写发送请求的方法，借助点击事件完成。选中对应的按钮`$("选择器")`，再去添加点击的事件，`$.ajax()`函数发送异步请求。

2.JQUery封装了一个函数，`$.ajax()`，`$.`是因为JQUery把`$`符号定义成对象。所以`$.ajax()`即通过对象调用`ajax()`函数，依靠Javascript提供的`XHR(XmlHttpResponse)`对象封装,可以异步加载相关的请求（独立刷新加载局部内容）。

3.`ajax()`使用方式。

- 需要传递一个方法体最为方法的参数使用，一对大括号称之为方法体。
- `ajax()`接收多个参数，参数与参数使用`,`进行分割，每一组参数之间使用`:`进行分割，参数的组成部分其一是参数的名称（不能随意定义），其后是参数的值，要求使用字符串（`""`）来表示。
- 参数的声明顺序没有要求

语法结构：

```javascript
$.ajax( {
  url:"",
  type:"",
  data:"",
  dataType:"",
  success:function(){
    
  },
  error:function(){
    
  }
} );
```

4.`ajax()`函数参数的含义：

|参数|功能描述|
|--|--|
|url|标识请求的url地址，不能包含参数列表部分的内容（~~`?username=Tom`~~）。例如：`url:"localhost:8080/users/reg"`|
|type|请求类型（GET和POST）。|
|data|向指定的请求url地址提交的数据。例如：`data:"username=Tom?&pwd=123`|
|dataType|提交的数据的类型。数据的类型一般指定为json类型。`dataType:"json"`|
|success|当服务器正常响应客户端时，会自动调用success参数的方法，并且将服务器返回的数据以对象（？参数）的形式传递给success|
|error|当服务器未正常响应客户端时，会自动调用error参数的方法，并且将服务器返回的数据以对象（？参数）的形式传递给error|

5.js代码可以独立存放在一个.js文件里或者声明在一个script标签中。

```javascript
<script type="text/javascript">
            // 1.监听注册按钮是否被点击，被点击可以执行一个方法
            $("#btn-reg").click(function () {
                // 动态获取表单中控制的数据(假设有id属性）
                //let username = $("#username").val();
                //let pwd = $("#password").val();
                //console.log( $("#form-reg").serialize() )

                // 2.发送ajax()的异步请求来完成用户注册功能
                $.ajax( {
                    url:"/users/reg",
                    type:"POST",
                    // 自动拼接：username=Tom&password=123
                    data:$("#form-reg").serialize(),
                    // data:"username="+username "&password="+pwd,
                    dataType:"JSON",
                    success:function(json){
                        if(json.state === 200){
                            alert("注册成功");

                        }else{
                            alert("注册失败");
                        }
                    },
                    error:function(xhr){
                        alert("注册时产生未知的错误！" + xhr.status);
                    }
                } );
            });
        </script>
```

6.js代码无法正常被服务器解析执行，体现在点击页面中的按钮没有任何的响应。

- 浏览器缓存
- idea
- - 在项目的maven下cleari清理项目-install重新部署
  - 在项目的file选项下-cash清理缓存
  - 重新的去构建项目：build选项下-rebuild选项
  - 重启idea
- 重启电脑

<br/>

## 用户登录

当用户输入用户名和密码将数据提交给后台数据库进行查询，如果存在对应的用户名和密码则表示登陆成功，登陆成功后跳转到系统的主页（index.html），跳转在前端使用jquery来完成。

### 1.登录-持久层

#### 1.1 规划需要执行的SQL语句

依据用户提交的用户名和密码做select查询。密码的比较在业务层执行。

```mysql
select * from t_user where username=?
```

> 说明：如果在分析过程发现某个功能模块已经被开发完成，就可以省略当前的开发步骤，这个分析过程不能够省略。

#### 1.2 接口设计和抽象方法

> 不用重复开发，单元测试也无需单独执行。

<br/>

### 2.登录-业务层

#### 2.1 规划异常

1.用户名对应的密码错误，密码匹配失败的异常：`PasswordNotMatchException`异常，运行时异常，业务异常。

2.用户名未找到，抛出异常：`UsernameNotFoundException`

3.异常的编写：

- 业务层需要继承`ServiceException`异常类；
- 在具体的异常类中定义构造方法（用快捷键生成5个构造方法）。

<br/>

#### 2.2 设计业务层的接口和抽象方法

1.直接在IUserService接口中编写抽象方法，`login(String username,String password)`。将当前登录成功的用户数据以当前用户对象的形式返回。

```java
/**
     * 用户登录方法
     * @param username 用户名
     * @param password 用户密码
     * @return 当前匹配的用户数据，如果没有返回null值
     */
    User login(String username,String password);
```

状态管理：将数据保存在cookie或session中==【待做】==。

- session：用户名、用户id
- cookie：用户头像

2.需要在实现类中实现父接口中抽象方法。

```javascript
@Override
    public User login(String username, String password) {
        // 根据用户名称查询用户数据是否存在，无则抛出异常
        User result = userMapper.findByUsername(username);
        if( result == null ){
            throw new UsernameNotFoundException("用户数据不存在");
        }
        // 判断is_delete字段值是否为1（是否标记为删除）
        if( result.getIsDelete() == 1 ){
            throw new UsernameNotFoundException("用户数据不存在");
        }

        /* 检测用户的密码是否匹配 */
        // 1.先获取到数据库中加密后的密码(这里演示用的oldPassword，不够明确）
        String dataPassword = result.getPassword();
        // 2.和用户传递过来的密码进行比较
        // 2.1 获取注册时生成的盐值
        String salt = result.getSalt();
        // 2.2 将用户传递的密码按照相同的md5算法规则进行加密
        String newMD5Password = getMD5Password(password,salt);
        // 3. 将密码进行比较
        if( !newMD5Password.equals(dataPassword) ){
            throw new PasswordNotMatchException("用户密码错误");
        }

        // 重新new User对象封装，只传uid、用户名和头像
        User user = new User();
        user.setUid(result.getUid());
        user.setUsername(result.getUsername());
        user.setAvatar(result.getAvatar());

        return user;
    }
```

3.在测试类中测试业务层登录的方法是否可以执行通过。记得给所有字段赋值，看是否只返回uid，username和头像（avatar）

```java
@Test
    public void login(){
        User user = userService.login("test01","123");
        System.out.println(user);
    }
```

<br/>

### 3.登录-控制层

### 3.1 处理异常

业务层抛出的异常是什么，需要在统一异常处理类中进行统一的捕获和处理，如果业务层抛出的异常类型已经在统一异常处理类中处理过，则不需要重复添加。

```java
// 新增的异常
else if(e instanceof UsernameNotFoundException){
    result.setState(5001);
    result.setMessage("用户数据不存在异常");
}
else if(e instanceof PasswordNotMatchException){
    result.setState(5002);
    result.setMessage("用户名或密码错误");
}
```

### 3.2 设计请求

```properties
请求路径:/users/login
请求方式:POST
请求数据:String username,String password，[+ 6.4.3 HttpSession session]
响应结果:JsonResult<User>
```

### 3.3处理请求 

在`UserController`类中编写处理请求的方法。

```java
// 处理登录请求
    @RequestMapping("login")
     public JsonResult<User> login(String username,String password){
         User data = userService.login(username,password);
         return new JsonResult<User>(OK,data);
    }
```

<br/>

### 4.登录-前端页面

1.在login.html页面中依据前面所设置的请求来发送ajax请求。

```javascript
<script type="text/javascript">
            $("#btn-login").click(function () {
                $.ajax( {
                    url:"/users/login",
                    type:"POST",
                    data:$("#form-login").serialize(),
                    dataType:"JSON",
                    success:function(json){
                        if(json.state == 200){
                            alert("登录成功");
                            location.href = "index.html";
                        }else{
                            alert("登录失败");
                            // console.log(json.state);
                        }
                    },
                    error:function(xhr){
                        alert("登录时产生未知的异常：" + xhr.message);
                    }
                } );
            });
        </script>
```

2.访问页面登录测试

<br/>

## 用户登录会话session

session对象主要存在服务器端，可以用于保存服务器的临时数据对象，所保存的数据在整个项目中都可以通过访问来获取，把session的数据看作一个共享的数据。首次登录的时候获取的用户的数据，转移到session对象即可。

`session.getAttrbute("key")`，可以将获取session中的数据这种行为进行封装，封装在`BaseController`类中。

1.封装session对象中数据的获取、数据的设置（当用户登录成功后进行数据的设置，设置到全局的session对象中）

2.在父类中封装两个数据：获取uid和username对应的两个方法。==用户头像暂时不考虑，将来封装在cookie中。==

```java
    /**
     * 获取session对象中的uid
     * @param session session对象
     * @return 当前登录的用户uid的值
     */
    protected final Integer getUidFromSession(HttpSession session){
        // 借助包装类将串转换成整数
        return Integer.valueOf( session.getAttribute("uid").toString() );
    }

    /**
     * 获取session对象中的username
     * @param session session对象
     * @return 当前登录的用户username
     *
     * 待update：在实现类中重写父类中的toString()方法，输出不是句柄信息
     */
    protected final String getUsernameFromSession(HttpSession session){
        return session.getAttribute("username").toString();
    }
```

3.在登录的方法中将数据封装在session对象中。服务器启动时会自动创建全局的session对象。

SpringBoot直接使用session对象，直接将HttpSession类型的对象作为请求处理方法的参数，会自动将全局的session对象注入到请求处理方法的session形参上。

补充6.3.2的设计请求部分

向session对象中完成数据的绑定

```java
// 处理登录请求
    @RequestMapping("login")
     public JsonResult<User> login(String username,
                                   String password,
                                   HttpSession session){
         User data = userService.login(username,password);

         // 向session对象中完成数据的绑定(session全局的)
         session.setAttribute( "uid",data.getUid() );
         session.setAttribute( "username",data.getUsername() );

         // 测试：获取session中绑定的数据
        System.out.println( getUidFromSession(session) );
        System.out.println( getUsernameFromSession(session) );

         return new JsonResult<User>(OK,data);
     }
```

<br/>

<br/>

## 过滤器和拦截器

首先将所有的请求统一拦截到栏截器中，在拦截器定义过滤规则。不满足则统一重新打开login.html页面（重定向和转发 - 重定向和转发都是资源的跳转，前者是在浏览器端跳转，会产生两次请求，地址栏url会变化，后者转发是在服务器端跳转，且只会产生一次请求，地址栏url不会变化），推荐使用重定向。

- 使用转发如果代码部署不在同一台服务器，很有可能访问出bug

在SpringBoot项目中拦截器的定义和使用：SpringBoot是依靠springMVC来完成的。SpringMVC提供了一个 `Handlerlnterceptor`接口，用于表示定义一个拦截器。
源码解析：

```java
/** 拦截在此进行*/
public interface HandlerInterceptor {
  // 在 处理 之前：在调用所有处理请求的方法之前被自动调用执行的方法
    default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

/** 资源的回收*/
// 在 处理 之后：在ModelAndview?对象返回之后被调用的方法
    default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    }


// 处理 完成时：在最后整个请求所有关联的资源被执行完毕所执行的方法
    default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
    }
}
```

<br/>

1.首先自定义一个`interceptor.LoginInterceptor`类，让这个类实现实现`Handlerlnterceptor`接口

```java
package com.cy.store.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 定义一个拦截器 */
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * pre回车出来
     * 检测全局session对象中是否有uid数据。有则放行，没有重定向到login.html
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器（url + Controller：映射）
     * @return 如果返回值为true表示放行当前的请求，如果返回值为false则表示拦截当前的请求
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 通过HttpServletRequest对象获取session全局对象
        Object obj = request.getSession().getAttribute("uid");
        // 用户未登录，重定向
        if (obj == null){
            response.sendRedirect("/web/login.html");
            // 结束后续的调用
            return false;
        }
        // 请求放行
        return true;
    }
}
```

==弹幕说讲解时老师把过滤器和拦截器搞混了：简单理解，这里老师说的注册过滤器其实就是拦截器配置类。不太清楚所以我都没改==

2.注册过滤器：添加白名单（哪些资源可以在不登录的情况下访问：login.html/register/login/reg/index/product.html）、添加黑名单（在用户登录的情况下才可以访问的页面资源）。

3.注册过滤器的技术：借助`WebMvcConfigure`接口，可以将用户定义的拦截器进行注册，保证拦截器能够生效和使用。

定义`config.LoginIntercaptorConfigurer`类，让其实现`WebMvcConfigure`接口。建议将配置信息存放在项目的config包结构下。

```java
// 将自定义拦截器进行注册
default void addInterceptors(InterceptorRegistry registry) {
  
}
```

4.测试：浏览器提示重定向次数过多，login.html页面也无法打开
