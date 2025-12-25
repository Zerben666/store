## 商品热销排行

### 1 商品-创建数据表

选中store数据库，创建t_produce数据表

### 2 商品-创建实体类

创建com.gy.store.entity.Product类，并继承自BaseEntity类。在类中声明与数据表中对应的属性。

==注意下划线和大小写==

```java
package com.cy.store.entity;

import java.io.Serializable;

public class Product extends BaseEntity implements Serializable {
    private Integer id;             //商品id
    private Integer categoryId;    //分类id
    private String itemType;       //商品系列
    private String title;           //商品标题
    private String sellPoint;      //商品卖点
    private Long price;             //商品单价
    private Integer num;            //库存数量
    private String image;           //图片路径
    private Integer status;         //商品状态  1：上架   2：下架   3：删除
    private Integer priority;       //显示优先级
    
    //getter和setter
    // toString
    // equals和hashCode
}
```

### 3 商品-热销排行-持久层

#### 3.1 规划SQL语句

查询热销商品列表的SQL语句大致是。

```mysql
SELECT * FROM t_product WHERE status=1 ORDER BY priority DESC LIMIT 0,4
```

#### 3.2 接口与抽象方法

在com.cy.store.mapper包下创建`ProductMapper`接口并在接口中添加查询热销商品findHotList()的方法。

```java
package com.cy.store.mapper;
import com.cy.store.entity.Product;
import java.util.List;

/** 处理商品数据的持久层接口*/
public interface ProductMapper {
    /**
     * 查询热销商品的前四名
     * @return 热销商品前四名的集合
     */
    List<Product> findHotList();
}

```

#### 3.3 配置SQL映射

1.在main\resources\mapper文件夹下创建ProductMapper.xml文件，并在文件中配置findHotList()方法的映射。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!--namespace属性：用于指定当前的映射文件和哪个接口进行映射，需要指定接口的文件路径，需要标注包的完整路径接口-->
<mapper namespace="com.cy.store.mapper.ProductMapper">
    <!--自定义映射规则：resultMap标签来完成映射规则的定义-->
    <resultMap id="ProductEntityMap" type="com.cy.store.entity.Product">
        <id column="id" property="id"/>
        <result column="category_id" property="categoryId"/>
        <result column="item_type" property="itemType"/>
        <result column="title" property="title"/>
        <result column="sell_point" property="sellPoint"/>
        <result column="price" property="price"/>
        <result column="num" property="num"/>
        <result column="image" property="image"/>
        <result column="status" property="status"/>
        <result column="priority" property="priority"/>
    </resultMap>

    <!-- 查询热销商品的前四名 -->
    <select id="findHotList" resultMap="ProductEntityMap">
        SELECT * FROM t_product WHERE status=1
        ORDER BY priority DESC LIMIT 0,4
    </select>
</mapper>
```

2.在`com.cy.store.mapper`包下创建`ProductMapperTests`测试类，并添加测试方法。

略

### 4 商品-热销排行-业务层

#### 4.1 规划异常

> 说明：无异常

#### 4.2 接口与抽象方法

创建`com.cy.store.service.IProductService`接口，并在接口中添加`findHotList()`方法。

```java
package com.cy.store.service;
import com.cy.store.entity.Product;
import java.util.List;

public interface IProductService {
    List<Product> findHotList();
}

```

#### 4.3 实现抽象方法

1.创建`com.cy.store.service.impl.ProductServiceImpl`类，并添加`@Service注`解：在类中声明持久层对象以及实现接口中的方法。

```java
package com.cy.store.service.impl;

import com.cy.store.entity.Product;
import com.cy.store.mapper.ProductMapper;
import com.cy.store.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> findHotList() {
        List<Product> list = productMapper.findHotList();
        for (Product product : list) {
            // 过滤冗余数据
            product.setPriority(null);
            product.setCreatedUser(null);
            product.setCreatedTime(null);
            product.setModifiedUser(null);
            product.setModifiedTime(null);
        }
        return list;
    }
}
```

2.在com.cy.store.service包下创建测试类ProductServiceTests,并编写测试方法。

略

### 5 商品-热销排行-控制器

#### 5.1 处理异常

> 说明：无异常

#### 5.2 设计请求

```properties
请求路径:/products/hot_list
请求方式:GET
请求数据:--
响应结果:JsonResult<List<Prodect>>
是否拦截：否，需要将index.html和products/**添加到白名单
```

2.在LoginlnterceptorConfigurer类中将index.html页面（已有）和products/**请求添加到白名单

```java
patterns.add("/products/**");
```

#### 5.3 处理请求

1.创建`com.cy.controller.ProductController`类继承自`BaseController`类，类添加`@RestController`和`@RequestMapping(products")`注解，并在类中添加业务层对象。

```java
package com.cy.store.Controller;

import com.cy.store.entity.Product;
import com.cy.store.service.IProductService;
import com.cy.store.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("products")
public class ProductController extends BaseController {
    @Autowired
    private IProductService productService;
}
```

2.在类中添加处理请求的getHotList()方法

```java
@RequestMapping("hot_list")
    public JsonResult<List<Product>> getHotList() {
        List<Product> data = productService.findHotList();
        return new JsonResult<List<Product>>(OK,data);
    }
```

3.启动项目，访问http://localhost:8080/products/hot_list

### 6 商品-热销排行-前端页面

1.在index.html页面给“热销排行"列表的div标签设置id属性值。

```html
<div class="panel-heading">
							<p class="panel-title">热销排行</p>
						</div>
						<div id="hot-list" class="panel-body panel-item">
```

2.在index.html页面中body标签内部的最后，添加展示热销排行商品的代码。

```javascript
<script type="text/javascript">
			$(document).ready(function() {
				showHotList();
			});

			function showHotList() {
				$("#hot-list").empty();
				$.ajax( {
					url:"/products/hot_list",
					type:"GET",
					dataType:"JSON",
					success:function(json){
						let list = json.data;
						console.log("count=" + list.length);
						for (let i = 0; i < list.length; i++) {
							let html = '<div class="col-md-12">\n' +
									'<div class="col-md-7 text-row-2"><a href="product.html?id=#{id}">#{title}</a></div>\n' +
									'<div class="col-md-2">¥#{price}</div>\n' +
									'<div class="col-md-3"><img src="..#{image}collect.png" class="img-responsive" /></div>\n' +
									'</div>';

							html = html.replace(/#{id}/g,list[i].id);
							html = html.replace(/#{title}/g,list[i].title);
							html = html.replace(/#{price}/g,list[i].price);
							html = html.replace(/#{image}/g,list[i].image);

							$("#hot-list").append(html);
						}
					},
					error:function(xhr){
						alert("产生未知的异常：" + xhr.message);
					}
				} );
			}
		</script>
```

3.完成后启动项目，访问http://localhost:8080/web/index.html，观察”热销排行“栏

## 显示商品详情

### 1 商品-显示商品详情-持久层

#### 1.1 规划需要执行的SQL语句

根据商品id显示商品详情的SQL语句大致是

```mysql
SELECT * FROM t_product WHERE id=?
```

#### 1.2 接口与抽象方法

在ProductMapper接口中添加抽象方法

```java
/**
     * 根据商品id查询
     * @param id 商品id
     * @return 匹配的商品详情
     */
    Product findById(Integer id);
```

#### 1.3 配置SQL映射

1.在ProductMapper.xml文件中配置`findById(Integer id)`方法的映射。

```xml
<!-- 根据商品id查询商品详情-->
    <select id="findById" resultMap="ProductEntityMap">
        SELECT * FROM t_product WHERE id=#{id}
    </select>
```

2.测试省略

<br/>

### 2 商品-显示商品详情-业务层

#### 2.1 规划异常

如果商品数据不存在，应该抛出ProductNotFoundException,需要创建 com.cy.store.service.ex.ProductNotFoundException异常。

```java
package com.cy.store.service.ex;

/** 商品数据不存在的异常 */
public class ProductNotFoundException extends ServiceException {
  ...
}
```

#### 2.2 接口与抽象方法

在业务层`IProductService`接口中添加`findById(Integer id)`抽象方法。

```java
/**
     * 根据商品id查询商品详情
     * @param id 商品id
     * @return 匹配的商品详情
     */
    Product findById(Integer id);
```

#### 2.3 实现抽象方法

1.在`ProductServicelmpl`类中，实现接口中的`findById(Integer id)`抽象方法。

```java
@Override
    public Product findById(Integer id) {
        // 根据参数id调用私有方法执行查询，获取商品数据
        Product product = productMapper.findById(id);
        // 判断查询结果是否为null
        if (product == null) {
            throw new ProductNotFoundException("尝试访问的商品数据不存在");
        }
        // 将查询结果的部分属性设置为null
        product.setPriority(null);
        product.setCreatedUser(null);
        product.setCreatedTime(null);
        product.setModifiedUser(null);
        product.setModifiedTime(null);

        return product;
    }
```

### 商品-显示商品详情-控制层

#### 3.1 处理异常

在`BaseController`类中的`handleException()`方法中添加处理`ProductNotFoundException`的异常

```java
else if(e instanceof ProductNotFoundException){
            result.setState(4006);
            result.setMessage("商品数据不存在的异常");
        }
```

#### 3.2 设计请求

```properties
请求路径:/products/{id}/details
请求方式:GET
请求数据:@PathVariable("id") Integer id
响应结果:JsonResult<Product>
```

#### 3.3 处理请求

1.在`ProductController`类中添加处理请求的`getById()`方法。

```java
@GetMapping("{id}/details")
    public JsonResult<Product> getById(@PathVariable("id") Integer id){
        // 调用业务对象执行获取数据
        Product data = productService.findById(id);
        return new JsonResult<>(OK,data);
    }
```

2.启动，访问http://localhost:8080/products/10000017/details

### 4 商品-显示商品详情-前端页面

1.检查在product.html页面body标签内部的最后是否引入jquery-getUrlParam.js文件，如果有无需重复引入（已有）。

```html
<script type="text/javascript" src="../js/jquery-getUrlParam.js"></script>
```

2.在product.html页面中body标签内部的最后添加获取当前商品详情的代码

```html
<script type="text/javascript">
			let id = $.getUrlParam("id");
			console.log("id=" + id);
			$(document).ready(function() {
				$.ajax( {
					url:"/products/" + id +"/details",
					type:"GET",
					dataType:"JSON",
					success:function(json){
						if(json.state == 200){
							console.log("title" + json.data.title);

							$("#product-title").html(json.data.title);
							$("#product-sell-point").html(json.data.sellPoint);
							$("#product-price").html(json.data.price);

							// 大小图5张
							for (let i = 1; i <= 5; i++) {
								$( "#product-image-" + i + "-big").attr("src",".." + json.data.image + i + "_big.png");
								$( "#product-image-" + i).attr("src",".." + json.data.image + i + ".jpg");
							}
						}else if(json.state == 4006) {
							alert("异常：商品数据不存在");
							location.href = "index.html";
						}else {
							alert("获取商品信息失败：" + json.message);
						}
					},
					error:function(xhr){
						alert("产生未知的异常：" + xhr.message);
					}
				} );
			});
		</script>
```

3.测试：http://localhost:8080/web/product.html?id=10000017