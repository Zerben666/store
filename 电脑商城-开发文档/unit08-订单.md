## 订单

### 1 数据库创建

```mysql
CREATE TABLE t_order (
	oid INT AUTO_INCREMENT COMMENT '订单id',
	uid INT NOT NULL COMMENT '用户id',
	recv_name VARCHAR(20) NOT NULL COMMENT '收货人姓名',
	recv_phone VARCHAR(20) COMMENT '收货人电话',
	recv_province VARCHAR(15) COMMENT '收货人所在省',
	recv_city VARCHAR(15) COMMENT '收货人所在市',
	recv_area VARCHAR(15) COMMENT '收货人所在区',
	recv_address VARCHAR(50) COMMENT '收货详细地址',
	total_price BIGINT COMMENT '总价',
	status INT COMMENT '状态：0-未支付，1-已支付，2-已取消，3-已关闭，4-已完成',
	order_time DATETIME COMMENT '下单时间',
	pay_time DATETIME COMMENT '支付时间',
	created_user VARCHAR(20) COMMENT '创建人',
	created_time DATETIME COMMENT '创建时间',
	modified_user VARCHAR(20) COMMENT '修改人',
	modified_time DATETIME COMMENT '修改时间',
	PRIMARY KEY (oid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE t_order_item (
	id INT AUTO_INCREMENT COMMENT '订单中的商品记录的id',
	oid INT NOT NULL COMMENT '所归属的订单的id',
	pid INT NOT NULL COMMENT '商品的id',
	title VARCHAR(100) NOT NULL COMMENT '商品标题',
	image VARCHAR(500) COMMENT '商品图片',
	price BIGINT COMMENT '商品价格',
	num INT COMMENT '购买数量',
	created_user VARCHAR(20) COMMENT '创建人',
	created_time DATETIME COMMENT '创建时间',
	modified_user VARCHAR(20) COMMENT '修改人',
	modified_time DATETIME COMMENT '修改时间',
	PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

### 2 创建实体类

1.订单实体类

```
package com.cy.store.entity;

import java.io.Serializable;
import java.util.Date;

/** 订单数据的实体类*/
public class Order extends BaseEntity implements Serializable {
    private Integer oid;
    private Integer uid;
    private String recvName;
    private String recvPhone;
    private String recvProvince;
    private String recvCity;
    private String recvArea;
    private String recvAddress;
    private Long totalPrice;
    private Integer status;
    private Date orderTime;
    private Date payTime;

    // getter和setter方法
    // toString
    // hashcode和equal
}
```

2.订单项实体类

```
package com.cy.store.entity;

import java.io.Serializable;

public class OrderItem extends BaseEntity implements Serializable {
    private Integer id;
    private Integer oid;
    private Integer pid;
    private String title;
    private String image;
    private Long price;
    private Integer num;

    // getter和setter方法
    // toString
    // hashcode和equal
}
```

### 3 持久层

#### 3.1 规划SQL

1.将数据插入到订单表中。

```mysql
INSERT INTO t_order (oid除外所有的字段) VALUES (字段的值)
```

2.将数据插入到订单项的表中。

```mysql
INSERT INTO t_order_item (id除外所有的字段) VALUES (字段的值)
```

#### 3.2 设计接口和抽象方法

创建一个`OrderMapper`接口，接口中添加以上两个SQL对象的抽象方法。

```java
package com.cy.store.mapper;

import com.cy.store.entity.Order;
import com.cy.store.entity.OrderItem;

/** 订单的持久层接口*/
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param order 订单数据
     * @return 影响的行数
     */
    Integer insertOrder(Order order);

    /**
     * 插入订单项的数据
     * @param orderItem 订单项数据
     * @return 影响的行数
     */
    Integer insertOrderItem(OrderItem orderItem);
}
```

#### 3.3 配置SQL映射

创建`OrderMapper.xml`的映射文件

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!--namespace属性：用于指定当前的映射文件和哪个接口进行映射，需要指定接口的文件路径，需要标注包的完整路径接口-->
<mapper namespace="com.cy.store.mapper.OrderMapper">
   <insert id="insertOrder" useGeneratedKeys="true" keyProperty="oid">
      INSERT INTO t_order (
         uid, recv_name, recv_phone, recv_province, recv_city, recv_area, recv_address, total_price,
         status, order_time, pay_time, created_user, created_time, modified_user, modified_time
      ) VALUES (
         #{uid}, #{recvName}, #{recvPhone}, #{recvProvince}, #{recvCity}, #{recvArea}, #{recvAddress}, #{totalPrice},
         #{status}, #{orderTime}, #{payTime}, #{createdUser}, #{createdTime}, #{modifiedUser}, #{modifiedTime}
      )
   </insert>

   <insert id="insertOrderItem" useGeneratedKeys="true" keyProperty="id">
      INSERT INTO t_order_item (
         id, oid, pid, title, image, price, num, created_user, created_time, modified_user,
         modified_time
      ) VALUES (
         #{id}, #{oid}, #{pid}, #{title}, #{image}, #{price}, #{num}, #{createdUser}, #{createdTime}, #{modifiedUser},
         #{modifiedTime}
      )
   </insert>
</mapper>
```

测试类

```
package com.cy.store.mappper;

// 使用JUnit 4测试

import com.cy.store.entity.Order;
import com.cy.store.entity.OrderItem;
import com.cy.store.mapper.OrderMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OrderMapperTests {

    @Autowired
    private OrderMapper orderMapper;
    @Test
    public void insertOrder() {
        Order order = new Order();
        order.setUid(22);
        order.setRecvName("明明");
        order.setRecvPhone("17857704444");
        orderMapper.insertOrder(order);
    }
    @Test
    public void insertOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setOid(1);
        orderItem.setPid(10000003);
        orderItem.setTitle("AAAA");
        orderMapper.insertOrderItem(orderItem);
    }
}
```

<br/>

### 4 业务层

1.在`IAddressService`接口中定义根据收货地址的id获取收货地址数据的方法。

```java
Address getByAid(Integer aid);
```

2.在子类中实现该抽象方法

```java
@Override
    public Address getByAid(Integer aid,Integer uid) {
        Address address = addressMapper.findByAid(aid);
        if (address == null) {
            throw new AddressNotFoundException("收货数据不存在");
        }
        if ( !address.getUid().equals(uid) ) {
            throw new AccessDeniedException("非法访问");
        }

        address.setProvinceCode(null);
        address.setCityCode(null);
        address.setAreaCode(null);
        address.setCreatedUser(null);
        address.setCreatedTime(null);
        address.setModifiedUser(null);
        address.setModifiedTime(null);
        return address;
    }
```

3.在service包下创建`IOrderService`接口，添加抽象方法用于创建订单。逻辑：从用户（UID）的购物车（aid）中选择商品进行结算。

```
public interface IOrderService {
    Order create(Integer aid,Integer uid,String username,Integer[] cids);
}

```

4.创建实现类`OrderServiceImple`类

```
package com.cy.store.service.impl;

import com.cy.store.entity.Address;
import com.cy.store.entity.Order;
import com.cy.store.entity.OrderItem;
import com.cy.store.mapper.OrderMapper;
import com.cy.store.service.IAddressService;
import com.cy.store.service.ICartService;
import com.cy.store.service.IOrderService;
import com.cy.store.service.ex.InsertException;
import com.cy.store.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImple implements IOrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private ICartService cartService;

    @Override
    public Order create(Integer aid, Integer uid, String username, Integer[] cids) {
        /* 即将要下单的列表 */
        List<CartVO> list = cartService.getVOByCid(uid,cids);
        // 计算商品总价
        Long totalPrice = 0L;
        for( CartVO c:list ){
            totalPrice += c.getRealPrice() * c.getNum();
        }

        Address address = addressService.getByAid(aid,uid);
        Order order = new Order();
        order.setUid(uid);

        // 收货地址数据
        order.setRecvName(address.getName());
        order.setRecvPhone(address.getPhone());
        order.setRecvProvince(address.getProvinceName());
        order.setRecvCity(address.getCityCode());
        order.setRecvArea(address.getAreaName());
        order.setRecvAddress(address.getAddress());
        // 支付、总价、提交时间
        order.setStatus(0);
        order.setTotalPrice(totalPrice);
        order.setOrderTime(new Date());
        // 日志
        order.setCreatedUser(username);
        order.setCreatedTime(new Date());
        order.setModifiedUser(username);
        order.setModifiedTime(new Date());

        // 查询数据
        Integer rows = orderMapper.insertOrder(order);
        if (rows != 1) {
            throw new InsertException("插入数据异常");
        }

        // 创建订单项的数据
        for( CartVO c:list ){
            // 创建订单项
            OrderItem orderItem = new OrderItem();
            // 补全数据
            orderItem.setOid(order.getOid());
            orderItem.setPid(c.getPid());
            orderItem.setTitle(c.getTitle());
            orderItem.setImage(c.getImage());
            orderItem.setPrice(c.getPrice());
            orderItem.setNum(c.getNum());
            // 日志字段
            orderItem.setCreatedUser(username);
            orderItem.setCreatedTime(new Date());
            orderItem.setModifiedUser(username);
            orderItem.setModifiedTime(new Date());
            // 插入数据操作
            orderMapper.insertOrderItem(orderItem);
            if (rows != 1) {
                throw new InsertException("插入数据异常");
            }
        }

        return order;
    }
}
```

5.测试

```java
package com.cy.store.service;

// 使用JUnit 4测试

import com.cy.store.entity.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OrderServiceTests {
    @Autowired
    private IOrderService orderService;
    @Test
    public void create(){
        Integer[] cids = {2,3};
        Order order = orderService.create(8,21,"彬",cids);
        System.out.println(order);
    }
}
```

<br/>

### 5 控制层

1.设计请求

```properties
请求路径:/orders/create
请求方式:POST
请求数据:Integer aid,Integer[] cids,HttpSession session
响应结果:JsonResult<Order>
```

2.创建`OrderController`类，并编写请求处理方法

```java
@RequestMapping("orders")
@RestController
public class OrderController extends BaseController {
    @Autowired
    private IOrderService orderService;

    @RequestMapping("create")
    public JsonResult<Order> create(Integer aid, Integer[] cids, HttpSession session){
        Integer uid = getUidFromSession(session);
        String username = getUsernameFromSession(session);
        Order data = orderService.create(aid,uid,username,cids);

        return new JsonResult<>(OK,data);
    }
}
```

### 6 前端页面

粗糙但能运行的代码

```javascript
<script type="text/javascript">
			$(document).ready(function () {
				showCartList();
				showAddressList();

				// 绑定创建订单按钮事件
				$("#btn-create-order").on("click", function() {
					createOrder();
				});
			});

			/* 创建订单 */
			$("#btn-create-order").click(function () {
				let aid = $("#address-list").val();   // 12
				let cids = location.search.substr(1); // cids=4&cids=3
				$.ajax({
					url: "/orders/create",
					type: "POST",
					data:"aid=" + aid + "&" + cids,  // aid=12&cids=4&cids=3
					dataType: "JSON",
					success: function (json) {
						if (json.state == 200){
							location.href = "payment.html";
							alert("订单创建成功");
							console.log(json.data);
						}
					},
					error: function (xhr) {
						alert("订单数据加载时产生未知的异常");
					}
				});
			});

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

			/* 显示购物车列表数据 */
			function showCartList() {
				$("#cart-list").empty();

				// 从URL中获取cids参数（格式为多个cids=3&cids=2）
				const params = new URLSearchParams(location.search);
				const cidsArray = params.getAll("cids"); // 获取所有cids参数值，返回数组

				if (!cidsArray || cidsArray.length === 0) {
					alert("未选择商品");
					return;
				}

				// 转换为整数数组（确保参数类型正确）
				const validCids = cidsArray.map(Number).filter(Number.isInteger);
				if (validCids.length === 0) {
					alert("无效的商品ID");
					return;
				}

				$.ajax({
					url: "/carts/list",
					type: "GET",
					data: location.search.substr(1),
					dataType: "JSON",
					success: function (json) {
						const list = json.data;
						let allCount = 0;
						let allPrice = 0;

						for (let i = 0; i < list.length; i++) {
							let tr = '<tr>\n' +
									'<td><img src="..#{image}collect.png" class="img-responsive" /></td>\n' +
									'<td>#{title}</td>\n' +
									'<td>¥<span>#{price}</span></td>\n' +
									'<td>#{num}</td>\n' +
									'<td><span>#{totalPrice}</span></td>\n' +
									'</tr>';
							tr = tr.replace(/#{image}/g, list[i].image);
							tr = tr.replace(/#{title}/g, list[i].title);
							tr = tr.replace(/#{price}/g, list[i].price);
							tr = tr.replace(/#{num}/g, list[i].num);
							tr = tr.replace(/#{totalPrice}/g, (list[i].price * list[i].num).toFixed(2));

							$("#cart-list").append(tr);

							// 统计总数量和总金额
							allCount += list[i].num;
							allPrice += list[i].price * list[i].num;
						}

						// 更新页面显示
						$("#all-count").text(allCount);
						$("#all-price").text(allPrice.toFixed(2));
					},
					error: function (xhr) {
						alert("加载商品列表失败: " + xhr.status);
					}
				});
			}

			// 创建订单并跳转到支付页面
			function createOrder() {
				// 获取选中的地址ID（这里假设使用第一个地址）
				const aid = $("select[name='aid']").val();

				// 从URL获取商品ID
				const params = new URLSearchParams(location.search);
				const cidsArray = params.getAll("cids");

				if (!aid || !cidsArray || cidsArray.length === 0) {
					alert("请选择收货地址和商品");
					return;
				}

				// 发送创建订单请求
				$.ajax({
					url: "/orders/create", // 假设的订单创建接口
					type: "POST",
					data: {
						aid: aid,
						cids: cidsArray // 传递原始数组（多个cids参数）
					},
					dataType: "JSON",
					success: function (json) {
						if (json.state === 200) {
							// 订单创建成功，跳转到支付页面
							alert("订单创建成功，即将跳转到支付页面");
							window.location.href = "payment.html?oid=" + json.data.oid;
						} else {
							alert("订单创建失败: " + json.message);
						}
					},
					error: function (xhr) {
						alert("创建订单失败: " + xhr.status);
					}
				});
			}
		</script>
```