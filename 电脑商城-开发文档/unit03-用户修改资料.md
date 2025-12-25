## 修改密码

需要用户提交原始密码和新密码，再根据当前登录的用户进行信息的修改操作。

### 1.修改密码-持久层

#### 1.1 规划需要执行的SQL语句

根据用户的uid修改用户password值。

```mysql
update t_user set password=?,modified_user=?,modified_time=? where uid=?
```

根据uid查询用户的数据。再修改密码之前，首先要保证当前用户的数据存在，检测是否被标记为已删除，检测输入的原始密码是否正确。

```mysql
SELECT * FROM t_user WHERE uid=?
```

#### 1.2设计接口和抽象方法

`UserMapper`接口，将以上两个设计定义出来，将来映射到sql语句上。

```java
    /**
     * 根据用户的uid修改用户密码
     * @param uid 用户的id
     * @param password 用户输入的新密码
     * @param modifiedUser 修改的执行者
     * @param modifiedTime 修改数据的时间
     * @return 返回值为受影响的行数
     */
    Integer updatePasswordByUid(Integer uid,
                                String password,
                                String modifiedUser,
                                Date modifiedTime);

    /**
     * 根据用户id来查询用户的数据
     * @param uid 用户的id
     * @return 如果找到对应的用户则返回其数据，反之返回null值
     */
    User findByUid(Inteager uid);
```

#### 1.3 SQL的映射

配置到映射文件UserMapper.xml中

```java
<update id="updatePasswordByUid">
        UPDATE t_user SET
            password=#{password},
            modified_user=#{modified_user},
            modified_time=#{modified_time}
        WHERE uid=#{uid}
    </update>

    <select id="findByUid" resultMap="UserEntityMap">
        SELECT * FROM t_user WHERE uid=#{uid}
    </select>
```

单元测试

```java
@Test
public void updatePasswordByUid(){
  userMapper.updatePasswordByUid( 14,"321","管理员",new Date() );
};
@Test
public void findByUid(){
  System.out.println(userMapper.findByUid(14));
};
```

<br/>

<br/>

### 2.修改密码-业务层

#### 2.1 规划可能产生的异常

1.用户的源密码错误、is_delete==1、uid找不到：`用户未发现异常`。

2.update在更新的时候，有可能产生未知的异常：`UpdateException`。

### 2.2 设计接口和抽象方法

执行用户修改密码的核心方法

```java
void changePassword(Integer uid,
                        String username,
                        String oldPassword,
                        String newPassword);
```

在实现类中实现当前的抽象方法。

```java
@Override
    public void changePassword(Integer uid, String username, String oldPassword, String newPassword) {
        User result = userMapper.findByUid(uid);
        if (result == null || result.getIsDelete() ==1) {
            throw new UsernameNotFoundException("用户数据不存在");
        }
        // 用户输入密码和数据库中密码进行比较
        String oldMd5Password = getMD5Password(oldPassword, result.getSalt());
        String newMd5Password = getMD5Password(newPassword, result.getSalt());
        if(!Objects.equals(oldPassword, newMd5Password)){
            throw new PasswordNotMatchException("密码错误");
        }
        /*if ( !result.getPassword().equals(oldMd5Password) ) {
            throw new PasswordNotMatchException("密码错误");
        }*/
        
        // 更新密码
        Integer rows = userMapper.updatePasswordByUid(
                                uid,newMd5Password,
                                username,new Date() );
        if(rows != 1){
            throw new UpdateException("更新数据时产生未知的异常");
        }
}
```

在单元测试中编写测试方法

```java
@Test
    public void updatePasswordByUid(){
        userMapper.updatePasswordByUid( 14,"3211","管理员",new Date() );
    };
    @Test
    public void findByUid(){
        System.out.println(userMapper.findByUid(14));
    };
```

<br/>

#### 踩坑

运行UserMapperTests.java文件下的updatePasswordByUid测试类报错:Parameter 'password' not found. Available parameters are [arg3, arg2, arg1, arg0, param3, param4, param1, param2]

这个错误是由于 MyBatis 在映射方法参数时无法识别参数名称导致的。具体原因和解决方案如下：
一、错误原因分析
在UserMapper.java的updatePasswordByUid方法中，参数没有使用@Param注解标识，导致 MyBatis 无法正确匹配 SQL 中的#{password}参数。
MyBatis 默认通过以下两种方式识别参数：
参数顺序（如arg0、arg1、param1、param2等）。
参数名称（需开启useActualParamName配置或通过@Param显式声明）。

在原来的代码中：
SQL 语句使用#{password}，期望匹配方法参数中的password。
但方法updatePasswordByUid的参数列表为：

```java
Integer updatePasswordByUid(Integer uid, String password, String modifiedUser, Date modifiedTime);
```

MyBatis 无法直接通过参数名称识别password，只能按顺序生成arg0（uid）、arg1（password）、arg2（modifiedUser）、arg3（modifiedTime）等占位符，导致#{password}无法匹配，抛出参数未找到的异常。

二、解决方案
方案 1：==在方法参数中添加@Param注解==
修改UserMapper.java中的方法定义，为每个参数添加@Param注解，明确参数名称：

```java
Integer updatePasswordByUid(
    @Param("uid") Integer uid,
    @Param("password") String password,
    @Param("modifiedUser") String modifiedUser,
    @Param("modifiedTime") Date modifiedTime
);
```

原理：通过@Param显式指定参数名，使 SQL 中的#{password}能正确匹配到@Param("password")修饰的参数。

方案 2：使用参数索引（不推荐，但临时可用）
将 SQL 中的参数占位符改为基于索引的名称（如arg1、param2等），匹配参数顺序：

```
<update id="updatePasswordByUid">
    UPDATE t_user SET
        password=#{arg1}, <!-- 对应方法参数中的第2个参数（password，索引从0开始） -->
        modified_user=#{arg2}, <!-- 对应第3个参数（modifiedUser） -->
        modified_time=#{arg3} <!-- 对应第4个参数（modifiedTime） -->
    WHERE uid=#{arg0} <!-- 对应第1个参数（uid） -->
</update>
```

缺点：参数索引不直观，代码可读性差，参数顺序变更时易出错，仅适用于临时调试。

更多有关信息访问`https://www.doubao.com/chat/7419503563650562`

<br/>

### 3.修改密码-控制层

#### 3.1处理异常

`UpdateException`需要配置在统一的异常处理方法中

```java
else if(e instanceof UpdateException){
            result.setState(5003);
            result.setMessage("更新数据时产生未知的异常");
        }
```

#### 3.2 设计请求

```properties
请求路径:/users/change_password
请求方式:POST
请求数据:String oldPassword,String newPassword,HttpSession session // 非pojo类型：需要和表单中的name属性值保持一致
响应结果:JsonResult<User>
```

#### 3.3 处理请求

```java
@RequestMapping(change_password)
    public JsonResult<Void> changePassword(String oldPassword,
                                           String newPassword,
                                           HttpSession session){
        Integer uid = getUidFromSession(session);
        String username = getUsernameFromSession(session);
        // 这里是调用哪个文件，视频里的名称好像不对
        userService.changePassword(uid,username,oldPassword,newPassword);
        return new JsonResult<>(OK);
    }
```

测试：登录后访问`http://localhost:8080/users/change_password?oldPassword=321&newPassword=123`，查看浏览器返回的json以及数据库md5密码是否更新成功

<br/>

#### 4.修改密码-前端页面

password.html中添加ajax请求的处理。

```java
<script type="text/javascript">
            $("#btn-change-password").click(function () {
                $.ajax( {
                    url:"/users/change_password",
                    type:"POST",
                    data:$("#form-change-password").serialize(),
                    dataType:"JSON",
                    success:function(json){
                        if(json.state == 200){
                            alert("密码修改成功");
                        }else{
                            alert("密码修改失败");
                            // console.log(json.state);
                        }
                    },
                    error:function(xhr){
                        alert("修改密码时产生未知的异常：" + xhr.message);
                    }
                } );
            });
        </script>
```

<br/>

<br/>

## 个人资料

### 1.个人资料-持久层

#### 1.1 需要规划SQL语句

1.更新用户信息的SQL语句

```mysql
UPDATE t_user 
SET phone=?,email=?,gender=?,mofified_user=?,modified_time=?
WHERE uid=?
```

2.session失效和拦截器未使用（普通版）

根据用户名查询用户的数据。

```mysql
SELECT * FROM t_user WHERE uid=?
```

> 查询用户的数据不需要再重复开发。

#### 1.2 接口与抽象方法

更新用户的信息方法的定义。

```java
/**
     * 更新用户的数据信息
     * @param user 用户的数据（电话号码、电子邮箱、性别三合一）
     * @return 受影响的行数
     */
    Integer updateInfoByUid(User user);
```

#### 1.3 抽象方法的映射

在UserMapper.xml文件中进行映射编写。

```xml
<update id="updateInfoByUid">
        UPDATE t_user SET
            // 加入非空判断
            <if test="phone!=null">phone = #{phone},</if>
            <if test="email!=null">email = #{email},</if>
            <if test="gender!=null">gender = #{gender},</if>
            modified_user=#{modifiedUser},
            modified_time=#{modifiedTime}
        WHERE uid = #{uid}
    </update>
```

在测试类中完成功能的测试

```java
@Test
    public void updateInfoByUid(){
        User user = new User();
        user.setUid(20);
        user.setPhone("15511110000");
        user.setEmail("test002@qq.com");
        user.setGender(1);
        userMapper.updateInfoByUid(user);
    }
```

<br/>

<br/>

### 2.个人资料-业务层

#### 2.1 异常规划

1.设计两个功能

- 当打开页面时获取用户的信息并填充到对应的文本框中
- 检测用户是否点击了修改按钮，如果检测到则执行修改用户信息的操作

2.打开页面的时候可能找不到用户的数据，点击删除按钮之前需要再次检测用户数据是否存在

#### 2.2 接口和抽象方法

主要有两个功能模块，对应的是两个抽象方法的设计。

```java
/**
     * 根据用户的id查询用户的数据
     * @param uid 用户id
     * @return 用户数据
     */
    User getByUid(Integer uid);

    /**
     * 更新用户的数据
     * @param uid 用户id
     * @param username 用户昵称
     * @param user 用户数据的对象（三合一）
     */
    void changeInfo(Integer uid,String username,User user);
```

#### 2.3 实现抽象方法

在`UserServiceImpl`类中添加两个抽象方法的具体实现。

```java
@Override
    public User getByUid(Integer uid) {
        User result = userMapper.findByUid(uid);
        if (result == null || result.getIsDelete()==1) {
            throw new UsernameNotFoundException("用户数据不存在");
        }
        User user = new User();
        user.setUsername(result.getUsername());
        user.setPhone(result.getPhone());
        user.setEmail(result.getEmail());
        user.setGender(result.getGender());

        return user;
    }

    /**
     * User对象中的数据phone,email,gender,手动将uid、username
     * 封装在user对象中
     *
     * @param uid 用户id
     * @param username 用户昵称
     * @param user 用户数据的对象（三合一）
     */
    @Override
    public void changeInfo(Integer uid, String username, User user) {
        User result = userMapper.findByUid(uid);
        if (result == null || result.getIsDelete()==1) {
            throw new UsernameNotFoundException("用户数据不存在");
        }

        // 对应
        user.setUid(uid);
        //user.setUsername(username());
        user.setModifiedUser(username);
        user.setModifiedTime(new Date());

        Integer rows = userMapper.updateInfoByUid(user);
        if (rows == 1) {
            throw new UpdateException("更新数据时产生未知的异常");
        }
    }
```

单元测试

```java
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
```

<br/>

<br/>

### 3.个人资料-控制层

#### 3.1 处理异常

> 暂无。

#### 3.2 设计请求

1.设置一打开页面就发送当前用户的查询。

```properties
请求路径:/users/get_by_uid
请求方式:GET
请求数据:HttpSession session
响应结果:JsonResult<User>
```

2.点击修改按钮发送用户的数据修改操作请求。

```properties
请求路径:/users/change_info
请求方式:POST
请求数据:User user,HttpSession session
响应结果:JsonResult<Void>
```

#### 3.3 处理请求

```java
// (打开页面就）发送当前用户的查询
     @RequestMapping("get_by_uid")
     public JsonResult<User> getByUid(HttpSession session){
        User data = userService.getByUid( getUidFromSession(session) );
        return new JsonResult<>(OK,data);
     }

     // （点击修改按钮）发送用户的数据修改操作请求
    @RequestMapping("change_info")
    public JsonResult<Void> changeInfo(User user,
                                       HttpSession session){
        // user对象有四部分数据：username、phone、email、gender
        // uid数据需要再次封装user对象中
        Integer uid = getUidFromSession(session);
        String username = getUsernameFromSession(session);
        userService.changeInfo(uid,username,user);
        return new JsonResult<>(OK);
    }
```

测试：登陆后访问1.`http://localhost:8080/users/get_by_uid?uid=20`

2.`http://localhost:8080/users/change_info?phone=123444555&email=test@qq.com&gender=0`

<br/>

#### 4.个人资料-前端页面

1.在打开userdata.html页面自动发送ajax请求，查询到的数据填充到这个页面。

```javascript
/**
		* 一旦检测到当前的页面被加载就会触发ready方法
		* [这个方法不能嵌套：放最后]
		*
		* $(document).ready( function() {
		* // 编写业务代码
		* });
		*/
		$(document).ready( function(){
		$.ajax( {
		url:"/users/get_by_uid",
		type:"GET",
		data:$("#form-change-info").serialize(),
		dataType:"JSON",
		success:function(json){
		if(json.state == 200){
		// 将查询到的用户数据填充到控件中
		$("#username").val(json.data.username);
		$("#phone").val(json.data.phone);
		$("#email").val(json.data.email);
							let radio = (json.data.gender == 0) ? $("#gender-female") : $("#gender-male");
							// prop()表示给某个元素添加属性及属性的值
							radio.prop("checked","checked");
						}else{
							alert("用户数据不存在");
							// console.log(json.state);
						}
					},
					error:function(xhr){
						alert("查询时产生未知的异常：" + xhr.message);
					}
				});
			});
```

2.检测到用户点击了修改按钮后发送一个ajax请求（change_info）。

```javascript
$("#btn-change-info").click(function () {
			$.ajax({
				url: "/users/change_info",
				type: "POST",
				data: $("#form-change-info").serialize(),
				dataType: "JSON",
				success: function (json) {
					if (json.state == 200) {
						alert("用户信息修改成功");
						location.href="userdata.html";
					} else {
						alert("用户信息修改失败");
						// console.log(json.state);
					}
				},
				error: function (xhr) {
					alert("修改时产生未知的异常：" + xhr.message);
				}
			});
		});
```