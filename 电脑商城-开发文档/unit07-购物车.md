## 加入购物车

### 1 数据创建

导入t_cart.sql

### 2 创建实体类

```java
package com.cy.store.entity;

public class Cart extends BaseEntity
    private Integer cid;
    private Integer uid;
    private Integer pid;
    private Long price;
    private Integer num;

    // getter和setter方法
    // toString
    // hashcode和equal
}
```

### 3 持久层

#### 3.1 规划需要执行的SQL语句

1.向购物车表中插入数据

```mysql
INSERT INTO t_cart (aid除外) VALUES (值列表)
```

2.如当前的商品已经在购物车中存在，则直接更新num的数量即可

```mysql
UPDATE t_cart set num=? where cid=?
```

3.在插入或者更新具体执行哪个语句，取决于数据库中没有当前的这个购物车商品的数据，得去查询才能确定

```mysql
SELECT * FROM t_cart WHERE uid=? AND pid=?
```

#### 3.2 设计接口和抽象方法

创建一个CartMapper接口持久层的文件

```java
package com.cy.store.mapper;
import com.cy.store.entity.Cart;
import java.util.Date;

public interface CartMapper {
    /**
     * 插入购物车数据
     * @param cart 购物车数据
     * @return 受影响的行数
     */
    Integer insert(Cart cart);

    /**
     * 更新购物车某件商品的数量
     * @param cid 购物车数据id
     * @param num 更新的数量
     * @param modifiedUser 修改者
     * @param modifiedTime 修改时间
     * @return 受影响的行数
     */
    Integer updateNumByCid(Integer cid, Integer num,
                           String modifiedUser, Date modifiedTime);

    /**
     * 根据用户id和商品id来查询购物车数据
     * @param uid 用户id
     * @param pid 商品id
     * @return 购物车数据
     */
    Cart findByUidAndPid(Integer uid,Integer pid);
}
```

#### 3.3 SQL映射

创建一个`CartMapper.xml`映射文件，添加以上三个抽象方法的SQL语句映射。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!--namespace属性：用于指定当前的映射文件和哪个接口进行映射，需要指定接口的文件路径，需要标注包的完整路径接口-->
<mapper namespace="com.cy.store.mapper.CartMapper">
    <!--自定义映射规则：resultMap标签来完成映射规则的定义-->
    <resultMap id="CartEntityMap" type="com.cy.store.entity.Cart">
        <id column="cid" property="cid"/>
        <result column="created_user" property="createdUser"/>
        <result column="created_time" property="createdTime"/>
        <result column="modified_user" property="modifiedUser"/>
        <result column="modified_time" property="modifiedTime"/>
    </resultMap>

    <!-- 1.向购物车表中插入数据 -->
    <insert id="insert" useGeneratedKeys="true" keyProperty="cid">
        INSERT INTO t_cart (uid, pid, price, num, created_user, created_time, modified_user, modified_time)
        VALUES (#{uid}, #{pid}, #{price}, #{num}, #{createdUser}, #{createdTime}, #{modifiedUser}, #{modifiedTime})
    </insert>

    <!-- 2.如当前的商品已经在购物车中存在，则直接更新num的数量即可 -->
    <update id="updateNumByCid">
        UPDATE t_cart
        SET num=#{num},modified_user=#{modifiedUser},modified_time=#{modifiedTime}
        WHERE cid=#{cid}
    </update>

    <!-- 3.在插入或者更新具体执行哪个语句，取决于数据库中没有当前的这个购物车商品的数据，得去查询才能确定 -->
    <select id="findByUidAndPid" resultMap="CartEntityMap">
        SELECT * FROM t_cart WHERE uid=#{uid} AND pid=#{pid}
    </select>
</mapper>
```

<br/>

#### 3.4单元测试

```java
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
import java.util.List;

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
}
```

参数名和实际参数对应出错，在 `CartMapper `接口里，为每个参数都添加上 `@Param` 注解，从而显式地给参数命名。

```java
/**
     * 更新购物车某件商品的数量
     * @param cid 购物车数据id
     * @param num 更新的数量
     * @param modifiedUser 修改者
     * @param modifiedTime 修改时间
     * @return 受影响的行数
     */
    Integer updateNumByCid(@Param("cid") Integer cid,
                           @Param("num") Integer num,
                           @Param("modifiedUser") String modifiedUser,
                           @Param("modifiedTime") Date modifiedTime);
```

findByUidAndPid()测试出错也是同理

<br/>

### 2 业务层

#### 2.1 规划异常

1.插入时可能产生异常：`InsertException`

2.更新数据时可能产生异常：`UpdateException`

#### 2.2 接口和抽象方法

创建一个`ICartService`接口文件

```java

```

### 2.3 实现接口

创建一个`CartServiceImpl`的实现类。

```java
package com.cy.store.service.impl;

import com.cy.store.entity.Cart;
import com.cy.store.entity.Product;
import com.cy.store.mapper.CartMapper;
import com.cy.store.mapper.ProductMapper;
import com.cy.store.service.ICartService;
import com.cy.store.service.ex.InsertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CartServiceImpl implements ICartService {
    /** 购物车的业务层依赖于其持久层和商品的持久层 */
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public void addToCArt(Integer uid, Integer pid, Integer amount, String username) {
        // 查询当前要添加的购物车数据是否在表中已存在
        Cart result = cartMapper.findByUidAndPid(uid,pid);
        Date date = new Date();
        if (result == null) { // 这个商品未被添加到购物车中,则进行新增操作
            // 创建一个cart对象
            Cart cart = new Cart();
            /* 补全对象数据：参数传递的数据 */
            cart.setUid(uid);
            cart.setPid(pid);
            cart.setNum(amount);    // 总数从前端获取
            // 补全价格：来自商品中的数据
            Product product = productMapper.findById(pid);
            cart.setPrice( product.getPrice() );
            // 补全4个日志
            cart.setCreatedUser(username);
            cart.setCreatedTime(date);
            cart.setModifiedUser(username);
            cart.setModifiedTime(date);

            // 执行数据插入操作
            Integer rows = cartMapper.insert(cart);
            if (rows != 1) {
                throw new InsertException("插入数据时产生未知的异常");
            }
        }else {    // 表示当前的商品在购物车中已经存在，则更新这条数据的num值
            Integer num = result.getNum() + amount;
            Integer rows = cartMapper.updateNumByCid(
                    result.getCid(),
                    num,
                    username,
                    date
            );
            if (rows != 1) {
                throw new InsertException("更新数据时产生未知的异常");
            }
        }
    }
}
```

创建对应的测试类`CartServiceTest`

```java
package com.cy.store.service;

// 使用JUnit 4测试

import com.cy.store.entity.Address;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CartServiceTest {
    /**
     * 单元测试方法：可以独立运行，不用启动整个顶目，提升了代码的测试效率
     * 1.必须被@Test注解修饰
     * 2.返回值类型必须是void
     * 3.方法的参数列表不指定任何类型
     * 4.方法的访问修饰符必须是public
     */

    @Autowired
    private ICartService cartService;
    /*@Test
    public void addToCard() {
        cartService.addToCArt( 21,10000003,5,"test" );
    }*/
    /*// 执行第一条后在执行相当于在存在的基础上更新
    @Test
    public void addToCard() {
        cartService.addToCArt( 21,10000003,5+2,"test" );
    }*/
    // 添加新的商品
    @Test
    public void addToCard() {
        cartService.addToCArt( 21,10000011,2,"test" );
    }
}
```

<br/>

### 3 控制层

1.没有需要处理的异常

2.请求处理

```properties
请求路径:/carts/add_to_cart
请求方式:GET
请求数据:Integer pid,Integer amount,HttpSession session
响应结果:JsonResult<User>
```

3.完成请求处理方法，创建`CartController`类

```java
package com.cy.store.Controller;

import com.cy.store.service.ICartService;
import com.cy.store.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RequestMapping("carts")
@RestController
public class CartController extends BaseController {
    @Autowired
    private ICartService cartService;

    @RequestMapping("add_to_card")
    public JsonResult<Void> addToCart(Integer pid,
                                      Integer amount,
                                      HttpSession session){
        cartService.addToCArt( getUidFromSession(session),
                pid,
                amount,
                getUsernameFromSession(session));
        return new JsonResult<>(OK);
    }
}
```

4.登录测试：http://localhost:8080/carts/add_to_cart?pid=10000012&amount=5

<br/>

### 4 前端页面

在product.html页面给[加入购物车]按钮添加点击事件并发送ajax请求

```javascript
$("#btn-add-to-cart").click(function () {
                $.ajax({
                    url: "/carts/add_to_cart",
                    type: "GET",
                    data:{
                        "pid":id,
                        "amount":$("#num").val()
                    },
                    dataType: "JSON",
                    success: function (json) {
                        if (json.state == 200){
                            alert("加入购物车成功");
                        }else{
                            alert("加入购物车失败：" + json.message);
                        }
                    },
                    error:function(xhr){
                        alert("加入购物车产生未知的异常：" + xhr.message);
                    }
                });
            })
```

在ajax函数中data参数的数据设置的方式：

- `data:$("form表单选择").serialize()`。当参数过多并且在同一个表单中，字符串的提交等。
- `data:new FormData( $("form表单选择")[0] )`。只适用提交文件。
- `data:"username=Tom"`。参数值固定且参数值列表有限，可以进行手动拼接。
  ```javascript
  // 这两种写法是一样的
  let user = "Tom"
  data:"username=" + user
  ```
- 使用JSON格式提交数据[本例使用]
  ```javascript
  data:{
    "username":"Tom",
    "age":18,
    "sex":0
  }
  ```

测试：访问http://localhost:8080/web/product.html?id=10000022，点击添加购物车，观察数据库新增记录

<br/>

## 显示购物车列表

### 1 持久层

#### 1.1 规划SQL语句

```mysql
SELECT 
    cid,uid,pid,
    t_cart.price,t_cart.num,
    t_product.title,t_product.image,
    t_product.price AS realprice
FROM 
    t_cart LEFT JOIN t_product ON t_cart.pid=t_product.id
WHERE uid=#{uid}
ORDER BY t_cart.created_time DESC
```

#### 1.2 设计接口和抽象方法

> VO:Value Object,值对象。当进行SELECT查询时，查询的结果数据属于**多张表中的内容**，此时发现结果集直接使用某个POJO实体类来接收，POJO实体类不能包含多表查询出来的结果。解决方式是：重新去构建一个新的对象，这个对象用于存储所查询出来的结果集对应的映射，所以把像这样的对象称之为**值对象**。

```java
package com.cy.store.vo;

import java.io.Serializable;

/** 表示购物车数据的VO类（Value Object）*/
public class CartVO implements Serializable {
    private Integer cid,uid,pid;
    private Long price;
    private Integer num;
    private String title,image;
    private Long realPrice;


    // getter/setter
    // toString
    // equals/hashCode
}
```

抽象方法设计

```java
List<CartVO> findVOByUid(Integer uid);
```

#### 1.3 映射SQL

```mysql
<select id="findVOByUid" resultType="com.cy.store.vo.CartVO">
        SELECT
        cid,uid,pid,
        t_cart.price,t_cart.num,
        t_product.title,t_product.image,
        t_product.price AS realprice
        FROM
        t_cart LEFT JOIN t_product ON t_cart.pid=t_product.id
        WHERE uid=#{uid}
        ORDER BY t_cart.created_time DESC
</select>
```

#### 1.4 单元测试

```java
@Test
    public void findOVByUid(){
        System.out.println( cartMapper.findVOByUid(21) );
    }
```

<br/>

### 2 业务层

1.先编写业务层的接口方法

```java
    List<CartVO> getVOByUid(Integer uid);
```

2.实现方法

```java
@Override
    public List<CartVO> getVOByUid(Integer uid) {
        return cartMapper.findVOByUid(uid);
    }
```

<br/>

### 3 控制层

1.设计请求

```properties
请求路径:/carts
请求方式:GET
请求数据:HttpSession session
响应结果:JsonResult<List<CartVO>>
```

2.实现请求

```java
 @RequestMapping({"","/"})
    public JsonResult<List<CartVO>> getVOByUid(HttpSession session){
        List<CartVO> data = cartService.getVOByUid(getUidFromSession(session));
        return new JsonResult<>(OK,data);
    }
```

3.测试：访问http://localhost:8080/carts

<br/>

### 4 前端页面

1.注释cart.js

```javascript
<!--<script src="../js/cart.js" type="text/javascript" charset="utf-8"></script>-->
```

2.form表单结构。action="orderConfirm.html"、tbody标签的id="cart-list"属性、“结算按钮"改成type="button"属性值。
3.ready()函数来完成自动的ajax请求的提交和处理

```javascript
$(document).ready(function () {
				showCartList();
			})

			/* 展示购物车列表数据 */
			function showCartList() {
				// 清空tbody标签中的数据
				$("#cart-list").empty();
				$.ajax({
					url: "/carts",
					type: "GET",
					dataType: "JSON",
					success: function (json) {
						let list = json.data;
						for (let i = 0; i < list.length; i++) {
							let tr = '<tr>\n' +
									'<td>\n' +
									'<input name="cids" value="#{cid}" type="checkbox" class="ckitem" />\n' +
									'</td>\n' +
									'<td><img src="..#{image}collect.png" class="img-responsive" /></td>\n' +
									'<td>#{title}#{msg}</td>\n' +
									'<td>¥<span id="goodsPrice#{cid}">#{singlePrice}</span></td>\n' +
									'<td>\n' +
									'<input id="price-#{cid}" type="button" value="-" class="num-btn" onclick="reduceNum(1)" />\n' +
									'<input id="goodsCount#{cid}" type="text" size="2" readonly="readonly" class="num-text" value="#{num}">\n' +
									'<input id="price+#{cid}" class="num-btn" type="button" value="+" onclick="addNum(1)" />\n' +
									'</td>\n' +
									'<td><span id="goodsCast#{cid}">#{totalPrice}</span></td>\n' +
									'<td>\n' +
									'<input type="button" onclick="delCartItem(this)" class="cart-del btn btn-default btn-xs" value="删除" />\n' +
									'</td>\n' +
									'</tr>';
							tr = tr.replace(/#{cid}/g,list[i].cid);
							tr = tr.replace(/#{image}/g,list[i].image);
							tr = tr.replace(/#{title}/g,list[i].title);
							tr = tr.replace(/#{msg}/g,list[i].realPrice);
							tr = tr.replace(/#{num}/g,list[i].num);
							tr = tr.replace(/#{singlePrice}/g,list[i].price);
							tr = tr.replace(/#{totalPrice}/g,list[i].price * list[i].num);

							$("#cart-list").append(tr);
						}
					},
					error: function (xhr) {
						alert("购物车列表数据加载产生未知的异常"+xhr.status);
					}
				});
			}
```

<br/>

## 增加购物车商品数量

### 1 持久层

#### 1.1 规划需要执行的SQL语句

1.执行更新t_cart表记录的num字段的值，无需重复开发

2.根据cid来查询当前购物车选中数据是否存在

```mysql
SELECT * FROM t_cart WHERE cid=#{cid}
```

#### 1.2 接口和抽象方法

```java
Cart findByCid(Integer cid);
```

#### 1.3 配置SQL映射

```java
<select id="findByCid" resultMap="CartEntityMap">
    SELECT * FROM t_cart WHERE cid=#{cid}
</select>
```

#### 1.4 单元测试

```java
@Test
    public void findByCId(){
        System.out.println(cartMapper.findByCid(1));
    }
```

<br/>

### 2 业务层

#### 2.1 规划异常

1.UpdateException已有

2.查询数据是否有访问的权限（已有）

3.要查询的数据不存在，抛出：`CartNotFoundException`

#### 2.2 设计接口和抽象方法

```java
/**
     * 更新用户的购物车数据的数量
     * @param cid
     * @param uid
     * @param username
     * @return 增加成功后新的数量
     */
    Integer addNum(Integer cid,Integer uid,String username);
```

#### 2.3 实现方法

```java
@Override
    public Integer addNum(Integer cid, Integer uid, String username) {
        Cart result = cartMapper.findByCid(cid);
        if (result == null) {
            throw new CartNotFoundException("数据不存在");
        }
        if( !result.getUid().equals(uid) ){
            throw new AccessDeniedException("数据非法访问");
        }
        Integer num = result.getNum() + 1;
        Integer rows = cartMapper.updateNumByCid(cid,num,username,new Date());
        if (rows != 1) {
            throw new UpdateException("更新数据失败");
        }
        // 返回新的购物车数据的总量
        return num;
    }
```

### 3 控制层

#### 3.1 处理异常

```java
else if(e instanceof CartNotFoundException){
            result.setState(4007);
            result.setMessage("购物车数据不存在的异常");
        }
```

#### 3.2 设计请求

```properties
请求路径:/carts/{cid}/add
请求方式:POST
请求数据:Integer cid, HttpSession session
响应结果:JsonResult<Integer>
```

#### 3.3 处理请求

```java
@RequestMapping("{cid}/num/add")
    public JsonResult<Integer> addNum(Integer cid,HttpSession session){
        Integer data = cartService.addNum(
                cid,
                getUidFromSession(session),
                getUsernameFromSession(session));
        return new JsonResult<>(OK,data);
    }
```

测试：http://localhost:8080/carts/7/num/add

<br/>

#### 4 前端页面

```javascript
<script type="text/javascript">
			$(document).ready(function () {
				showCartList();
			})

			/* 展示购物车列表数据 */
			function showCartList() {
				// 清空tbody标签中的数据
				$("#cart-list").empty();
				$.ajax({
					url: "/carts",
					type: "GET",
					dataType: "JSON",
					success: function (json) {
						let list = json.data;
						for (let i = 0; i < list.length; i++) {
							let tr = '<tr>\n' +
									'<td>\n' +
									'<input name="cids" value="#{cid}" type="checkbox" class="ckitem" />\n' +
									'</td>\n' +
									'<td><img src="..#{image}collect.png" class="img-responsive" /></td>\n' +
									'<td>#{title}#{msg}</td>\n' +
									'<td>¥<span id="goodsPrice#{cid}">#{singlePrice}</span></td>\n' +
									'<td>\n' +
									'<input id="price-#{cid}" type="button" value="-" class="num-btn" onclick="reduceNum(1)" />\n' +
									'<input id="goodsCount#{cid}" type="text" size="2" readonly="readonly" class="num-text" value="#{num}">\n' +
									'<input id="price+#{cid}" class="num-btn" type="button" value="+" onclick="addNum(#{cid})" />\n' +
									'</td>\n' +
									'<td><span id="goodsCast#{cid}">#{totalPrice}</span></td>\n' +
									'<td>\n' +
									'<input type="button" onclick="delCartItem(this)" class="cart-del btn btn-default btn-xs" value="删除" />\n' +
									'</td>\n' +
									'</tr>';
							tr = tr.replace(/#{cid}/g,list[i].cid);
							tr = tr.replace(/#{image}/g,list[i].image);
							tr = tr.replace(/#{title}/g,list[i].title);
							tr = tr.replace(/#{msg}/g,list[i].realPrice);
							tr = tr.replace(/#{num}/g,list[i].num);
							tr = tr.replace(/#{singlePrice}/g,list[i].price);
							tr = tr.replace(/#{totalPrice}/g,list[i].price * list[i].num);

							$("#cart-list").append(tr);
						}
					},
					error: function (xhr) {
						alert("购物车列表数据加载产生未知的异常"+xhr.status);
					}
				});
			}

			function addNum(cid) {
				$.ajax({
					url: "/carts/" + cid +"/num/add",
					type: "POST",
					dataType: "JSON",
					success: function (json) {
						if (json.state == 200){
							$("#goodsCount" + cid).val(json.data);
							// html()获取某个标签内部的内容
							let price = $("#goodsPrice" + cid).html();
							let totalPrice = price * json.data;
							$("#goodsCast" + cid).html(totalPrice);
						}else{
							alert("增加购物车数据失败" + json.message);
						}
					},
					error: function (xhr) {
						alert("加载购物车商品数量时产生未知的异常"+xhr.status);
					}
				});
			}

			/*$(function() {
				//返回链接
				$(".link-account").click(function() {
					location.href = "orderConfirm.html";
				})
			})*/
		</script>
```

<br/>

## 显示勾选的购物车结算数据

### 1 持久层

#### 1.1 规划SQL语句

用户在购物车列表页中通过随机勾选相关的商品，在点击”结算”按钮后，跳转到结算页面，在这个页面中需要展示用户在上个页面所勾选的购物车对应的数据。

列表的展示，而展示的内容还是购物车的表。两个页面需要用户勾选的多个cid传递给下一个页面。

```mysql
SELECT 
    cid,uid,pid,
    t_cart.price,t_cart.num,
    t_product.title,t_product.image,
    t_product.price AS realprice
FROM 
    t_cart LEFT JOIN t_product ON t_cart.pid=t_product.id
WHERE cid IN (?,?,?)
ORDER BY t_cart.created_time DESC
```

#### 1.2 接口和抽象方法

```java
List<CartVO> findVOByCid(Integer[] cids);
```

#### 1.3 配置映射

```java
    <select id="findVOByCid" resultType="com.cy.store.vo.CartVO">
        SELECT
            cid,uid,pid,
            t_cart.price,t_cart.num,
            t_product.title,t_product.image,
            t_product.price AS realprice
        FROM
            t_cart LEFT JOIN t_product ON t_cart.pid=t_product.id
        WHERE cid IN (
                    <foreach collection="array" item="cid" separator=",">
                        #{cid}
                    </foreach> )
        ORDER BY t_cart.created_time DESC
    </select>
```

#### 1.4 单元测试

```java
@Test
    public void findVOByCid(){
        Integer[] cids = {1,3,5,7};
        System.out.println(cartMapper.findVOByCid(cids));
    }
```

<br/>

### 2 业务层

1.无异常规划

2.设计业务层接口的抽象方法

```java
List<CartVO> getVOByCid(Integer uid,Integer[] cids);
```

3.完成抽象方法设计

```java
@Override
    public List<CartVO> getVOByCid(Integer uid, Integer[] cids) {
        // 检测是否正常拿到数据
        List<CartVO> list = cartMapper.findVOByCid(cids);
        Iterator<CartVO> it = list.iterator();
        while (it.hasNext()){
            CartVO cartVO = it.next();
            if (!cartVO.getUid().equals(uid)) { // 表示选中数据不属于当前的用户
                // 从集合中移除这个元素
                list.remove(cartVO);
            }
        }

        return list;
    }
```

<br/>

### 3 控制层

1.请求设计

```properties
请求路径:/carts/list
请求方式:POST
请求数据:Integer cids,HttpSession session
响应结果:JsonResult<List<CartVO>>
```

2.请求处理方法定义和声明

```java
@RequestMapping("list")
    public JsonResult<List<CartVO>> getVOByCid(Integer[] cids,HttpSession session){
        List<CartVO> data = cartService.getVOByCid( getUidFromSession(session),cids );
        return new JsonResult<>(OK,data);
    }
```

测试：http://localhost:8080/carts/?cids=1&cids=3

### 4 前端页面

#### 4.1 增加购物车商品数量

1.将cart.html页面中“结算"按钮属性更改成type="submit"

2.orderConfirm.html页面中添加自动加载从上个页面中传递过来的cids数据

<br/>

此处完善了之前的内容，重新弄了几次，大致如下：

一、补充减号按钮功能（cart.html）

1. 前端 JavaScript 补充 reduceNum 函数
在 cart.html 的 <script> 标签中添加以下代码：

```javascript
function reduceNum(cid) {
    // 获取当前数量
    let currentNum = parseInt($("#goodsCount" + cid).val());
    if (currentNum <= 1) {
        alert("商品数量不能小于 1");
        return;
    }

    $.ajax({
        url: "/carts/" + cid + "/num/reduce", // 后端接口路径
        type: "POST",
        dataType: "JSON",
        success: function (json) {
            if (json.state === 200) {
                // 更新数量和金额
                $("#goodsCount" + cid).val(json.data);
                const price = parseFloat($("#goodsPrice" + cid).text());
                const totalPrice = price * json.data;
                $("#goodsCast" + cid).text(totalPrice.toFixed(2));
                // 更新总价
                updateTotalPrice();
            } else {
                alert("减少数量失败：" + json.message);
            }
        },
        error: function (xhr) {
            alert("请求失败，状态码：" + xhr.status);
        }
    });
}

// 更新总价和已选数量（新增或修改）
function updateTotalPrice() {
    let totalCount = 0;
    let totalPrice = 0;
    // 遍历所有选中的复选框
    $(".ckitem:checked").each(function () {
        const cid = $(this).val();
        const count = parseInt($("#goodsCount" + cid).val());
        const price = parseFloat($("#goodsPrice" + cid).text());
        totalCount += count;
        totalPrice += count * price;
    });
    // 更新页面显示
    $("#selectCount").text(totalCount);
    $("#selectTotal").text(totalPrice.toFixed(2));
}

// 全选/反选功能（完善）
function checkall(checkbox) {
    $(".ckitem").prop("checked", checkbox.checked);
    updateTotalPrice(); // 全选时更新总价
}

// 为减号按钮绑定点击事件（修改原 HTML 中的按钮）
// 在购物车表格的减号按钮中添加 onclick 事件：
// <input type="button" value="-" class="num-btn" onclick="reduceNum(#{cid})" />
```

<br/>

2. 修改购物车表格的减号按钮 HTML
在 cart.html 的 <tbody> 模板中，将减号按钮的 onclick 改为传递 cid：

```
<td>
    <input type="button" value="-" class="num-btn" onclick="reduceNum(#{cid})" />
    <input id="goodsCount#{cid}" type="text" size="2" readonly class="num-text" value="#{num}">
    <input type="button" value="+" class="num-btn" onclick="addNum(#{cid})" />
</td>
```

二、后端实现减号功能

1. Controller 层（CartController.java）
添加减号对应的接口：
   ```java
   @RequestMapping("{cid}/num/reduce")
   public JsonResult<Integer> reduceNum(
       @PathVariable("cid") Integer cid,
       HttpSession session) {
       Integer data = cartService.reduceNum(
           cid,
           getUidFromSession(session),
           getUsernameFromSession(session)
       );
       return new JsonResult<>(OK, data);
   }
   ```

<br/>

2. Service 接口（ICartService.java）
添加 reduceNum 方法声明：

```java
Integer reduceNum(Integer cid, Integer uid, String username);
```

<br/>

3. Service 实现类（CartServiceImpl.java）
实现减少数量的逻辑：

```
@Override
public Integer reduceNum(Integer cid, Integer uid, String username) {
    // 查询购物车项
    Cart cart = cartMapper.findByCid(cid);
    if (cart == null) {
        throw new CartNotFoundException("购物车数据不存在");
    }
    // 校验用户权限
    if (!cart.getUid().equals(uid)) {
        throw new AccessDeniedException("非法访问他人购物车");
    }
    // 确保数量不小于 1
    int newNum = Math.max(1, cart.getNum() - 1);
    // 更新数量
    int rows = cartMapper.updateNumByCid(cid, newNum, username, new Date());
    if (rows != 1) {
        throw new UpdateException("更新数量失败");
    }
    return newNum;
}
```

三、修复cart.html和orderConfirm.html

略

<br/>

#### 4.2 确认订单页显示收货地址列表-前端页面

1.收货地址存放在一个select下拉列表中，将查询到的当前登录用户的收货地址动态的加载到这个下拉列表中。已经编写了根据用户的uid来查询当前用户的收货地址数据。

2.`OrderConfirm.html`页面中，收货地址数据的展示需要自动进行加载，需要将方法的逻辑放在`ready()`函数中。

```
$(document).ready(function () {
				showCartList();
				showAddressList();
				...
				});
```

3.声明和定义`showAddressList()`方法，方法中发送ajax请求即可。

```javascript
/* 显示收货地址列表 */
			function showAddressList() {
				$("#cart-list").empty();

				$.ajax({
					url: "/addresses",
					type: "GET",
					dataType: "JSON",
					success: function (json) {
						if (json.state == 200){
							let list = json.data;
							for (let i = 0; i < list.length; i++) {
								let opt = "<option value='#{aid}'>#{name}&nbsp;&nbsp;&nbsp;#{tag}&nbsp;&nbsp;&nbsp;#{provinceName}#{cityName}#{areaName}#{address}&nbsp;&nbsp;&nbsp;#{phone}</option>";

								// 复用：电话号码格式化（隐藏中间4位）
								let phone = list[i].phone || ''; // 处理空值
								if (phone.length >= 8) { // 至少8位才处理（前4+后4）
									phone = phone.slice(0, 4) + '***' + phone.slice(-4); // 前4位 + *** + 后4位
								} else {
									phone = phone.replace(/\d/g, '*'); // 短号码全隐藏（可选）
								}

								// 正则表达式替换，添加全局匹配标志g
								opt = opt.replace(/#{tag}/g, list[i].tag)
										.replace(/#{name}/g, list[i].name)
										.replace(/#{phone}/g, phone)
										.replace(/#{provinceName}/g,"【" +  list[i].provinceName)
										.replace(/#{cityName}/g, list[i].cityName)
										.replace(/#{areaName}/g, list[i].areaName + "】")
										.replace(/#{address}/g, list[i].address);

								opt = opt.replace("#{aid}",list[i].aid);

								// 追加
								$("#address-list").append(opt);
							}
						}
					},
					error: function (xhr) {
						alert("加载商品列表失败: " + xhr.status);
					}
				});
			}
```

测试：http://localhost:8080/web/orderConfirm.html?cids=3