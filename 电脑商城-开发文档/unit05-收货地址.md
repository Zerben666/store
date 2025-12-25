## 新增收货地址

### 1 新增收货地址-数据表的创建

```mysql
CREATE TABLE t_address (
    aid INT AUTO_INCREMENT COMMENT '收货地址id',
    uid INT COMMENT '归属的用户id',
    name VARCHAR(20) COMMENT '收货人姓名',
    province_name VARCHAR(15) COMMENT '省-名称',
    province_code CHAR(6) COMMENT '省-行政代号',
    city_name VARCHAR(15) COMMENT '市-名称',
    city_code CHAR(6) COMMENT '市-行政代号',
    area_name VARCHAR(15) COMMENT '区-名称',
    area_code CHAR(6) COMMENT '区-行政代号',
    zip CHAR(6) COMMENT '邮政编码',
    address VARCHAR(50) COMMENT '详细地址',
    phone VARCHAR(20) COMMENT '手机',
    tel VARCHAR(20) COMMENT '固话',
    tag VARCHAR(6) COMMENT '标签',
    is_default INT COMMENT '是否默认：0-不默认，1-默认',
    created_user VARCHAR(20) COMMENT '创建人',
    created_time DATETIME COMMENT '创建时间',
    modified_user VARCHAR(20) COMMENT '修改人',
    modified_time DATETIME COMMENT '修改时间',
    PRIMARY KEY (aid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

<br/>

### 2 新增收货地址-创建实体类

创建一个Address类，在类中定义表的相关字段，采用驼峰命名方式，再继承BaseEntity类

```java
public class Address extends BaseEntity{
    private Integer aid;
    private Integer uid;
    private String name;
    private String provinceName;
    private String provinceCode;
    private String cityName;
    private String cityCode;
    private String areaName;
    private String areaCode;
    private String zip;
    private String address;
    private String phone;
    private String tel;
    private String tag;
    private Integer isDefault;

    // Getter和Setter
    // equals和hashCode
    // toString
}
```

### 3 新增收货地址-持久层

#### 3.1 各功能的开发顺序

当前收货地址功能模块：[2]列表的展示、[5]修改、[4]删除、[3]设置默认、[1]新增收货地址。

<br/>

#### 3.2 规划需要执行的5QL语句

1.对应插入语句

```mysql
INSERT INTO t_address (除了aid外的字段列表) VALUES (字段值列表)
```

2.一个用户的收货地址规定最多只能有20条数据对应。再插入用户数据之前先查询当前用户的地址条数。==收获地址逻辑控制方面的异常。==

```mysql
SELECT count(*) FROM t_address WHERE uid=?
```

<br/>

#### 3.3 接口与抽象方法

1.创建一个接口Address，在这个接口中定义上面两个SQL语句抽象方法的定义。

```java
package com.cy.store.mapper;

import com.cy.store.entity.Address;

/** 收货地址持久层的接口 */
public interface AddressMapper {
    /**
     * 插入用户的收货地址数据
     * @param address 收货地址数据
     * @return 受影响的行数
     */
    Integer insert(Address address);

    /**
     * 根据用户id统计收获地址数量
     * @param uid 用户id
     * @return 当前用户的收货地址总数
     */
    Integer countByUid(Integer uid);
}

```

#### 3.4 配置SQL映射

创建一个AddressMapper.xml映射文件，在此添加到SQL语句的抽象方法的映射。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!--namespace属性：用于指定当前的映射文件和哪个接口进行映射，需要指定接口的文件路径，需要标注包的完整路径接口-->
<mapper namespace="com.cy.store.mapper.AddressMapper">
    <!--自定义映射规则：resultMap标签来完成映射规则的定义-->
    <resultMap id="AddressEntityMap" type="com.cy.store.mapper.AddressMapper">
        <id column="aid" property="aid"/>
        <result column="province_code" property="provinceCode"/>
        <result column="province_name" property="provinceName"/>
        <result column="city_code" property="cityCode"/>
        <result column="city_name" property="cityName"/>
        <result column="area_code" property="areaCode"/>
        <result column="area_name" property="areaName"/>
        <result column="is_default" property="isDefault"/>
        <result column="created_user" property="createdUser"/>
        <result column="created_time" property="createdTime"/>
        <result column="modified_user" property="modifiedUser"/>
        <result column="modified_time" property="modifiedTime"/>
    </resultMap>

    <insert id="insert" useGeneratedKeys="true" keyProperty="aid">
        INSERT INTO t_address (
            uid,name,province_name,province_code,city_name,city_code,
            area_name,area_code,zip,address,phone,tel,tag,is-default,created_user,
            created_time,modified_user,modified_time) VALUES (
                #{uid},#{name},#{provinceName},#{provinceCode},# {cityName},#{cityCode},#{areaName},
                #{areaCode},#zip},#{address},#{phone},#{tel},#{tag}, #{isDefault},#{createdUser},
                #{createdTime},#{modifiedUser},#{modifiedTime}
        )
    </insert>

    <select id="countByUid" resultType="java.lang.Integer">
        SELECT count(*) FROM t_address WHERE uid=#{uid}
    </select>
</mapper>
```

<br/>

单元测试：在test下的mapper文件夹下创建`AddressMapperTests`的测试类。

```
@SpringBootTest
@RunWith(SpringRunner.class)
public class AddressMapperTests {
    /**
     * 单元测试方法：可以独立运行，不用启动整个顶目，提升了代码的测试效率
     * 1.必须被@Test注解修饰
     * 2.返回值类型必须是void
     * 3.方法的参数列表不指定任何类型
     * 4.方法的访问修饰符必须是public
     */

    @Autowired
    private AddressMapper addressMapper;
    @Test
    public void insert() {
        Address address = new Address();
        address.setUid(21);
        address.setPhone("17858802974");
        address.setName("测试");
        address.setAddress("详细地址");
        address.setCreatedUser("admin");
        address.setCreatedTime(new Date()); 
        addressMapper.insert(address);
    }
    @Test
    public void countByUid(){
        Integer count = addressMapper.countByUid(21);
        System.out.println(count);
    }
}
```

### 4 新增收货地址-业务层

#### 4.1 规划异常

如果用户是第一次插入收货地址，规则：当用户插入的地址是第一条时，需要将当前地址作为默认的收货地址，如果查询到统计总数为0则将当前地址的is_default值设置为1。查询统计的结果为0不代表异常。

查询到的结果大于20了，这时候需要抛出业务控制的异常`AddressCountLimitException`异常。自行创建这个异常。

```java
/** 收货地址总数超限异常（>20条）*/
public class AddressCountLimitException extends ServiceException {
  // ...
}
```

插入数据时产生未知的异常InsertException，不需重复创建。

<br/>

#### 4.2 接口与抽象方法

1.创建一个`IAddressService`接口，其中定义业务的抽象方法

```java
package com.cy.store.service;
import com.cy.store.entity.Address;

/** 收货地址业务层接口 */
public interface IAddressService {
    Void addNewAddress(Integer uid, String username, Address address);
}

```

<br/>

2.创建一个`AddressServiceImpl`实现类，去实现接口中抽象方法。
在配置文件中定义数据。

```properties
# Spring读取配置文件中的数据：@Value("${user.address.max-count}")
user.address.max-count = 20
```

在实现类中实现业务控制

```java
package com.cy.store.service.impl;

import com.cy.store.entity.Address;
import com.cy.store.mapper.AddressMapper;
import com.cy.store.service.IAddressService;
import com.cy.store.service.ex.AddressCountLimitException;
import com.cy.store.service.ex.InsertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

/** 新增收货地址的实现类*/
public class AddressServiceImpl implements IAddressService {
    @Autowired
    private AddressMapper addressMapper;

    @Value("${user.address.max-count}")
    private Integer maxCount;

    @Override
    public Void addNewAddress(Integer uid, String username, Address address) {
        // 调用收货地址统计的方法
        Integer count = addressMapper.countByUid(uid);
        if (count >= maxCount) {
            throw new AddressCountLimitException("收货地址超出上限");
        }

        // uid、isDefault
        address.setUid(uid);
        Integer isDefault = count == 0 ? 1 : 0; // 1表示默认
        address.setIsDefault(isDefault);
        // 补全4项日志
        address.setCreatedUser(username);
        address.setModifiedUser(username);
        address.setCreatedTime(new Date());
        address.setModifiedTime(new Date());

        // 插入收货地址的方法
        Integer rows = addressMapper.insert(address);
        if (rows != 1) {
            throw new InsertException("插入地址时产生未知的异常");
        }
    }
}

```

3.测试业务层功能是否正常。`AddressServiceTests`

```java
package com.cy.store.service;

// 使用JUnit 4测试

import com.cy.store.entity.Address;
import com.cy.store.entity.User;
import com.cy.store.service.ex.ServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AddressServiceTests {
    /**
     * 单元测试方法：可以独立运行，不用启动整个顶目，提升了代码的测试效率
     * 1.必须被@Test注解修饰
     * 2.返回值类型必须是void
     * 3.方法的参数列表不指定任何类型
     * 4.方法的访问修饰符必须是public
     */

    @Autowired
    private IAddressService addressService;
    @Test
    public void addNewAddress(){
        Address address = new Address();
        address.setUid(21);
        address.setPhone("17858809999");
        address.setName("测试2");
        addressService.addNewAddress( 21,"管理员",address );
    }
}
```

<br/>

### 5 新增收货地址-控制层

#### 5.1 处理异常

业务层抛出了收货地址总数超标的异常，需要在`BaseController`中进行处理。

```java
else if(e instanceof AddressCountLimitException){
    result.setState(4003);
    result.setMessage("收货地址超出上限的异常");
}
```

#### 5.2 设计请求

```properties
请求路径:/addresses/add_new_address
请求方式:POST
请求数据:Address address,HttpSession session
响应结果:JsonResult<Void>
```

#### 5.3 处理请求

在控制层创建AddressController来处理用户收货地址的请求和响应。

```java
package com.cy.store.Controller;

import com.cy.store.entity.Address;
import com.cy.store.service.IAddressService;
import com.cy.store.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RequestMapping("addresses")
@RestController
public class AddressController extends BaseController {
    @Autowired
    private IAddressService addressService;

    @RequestMapping("add_new_address")
    public JsonResult<Void> addNewAddress(Address address, HttpSession session){
        Integer uid = getUidFromSession(session);
        String username = getUsernameFromSession(session);
        addressService.addNewAddress(uid,username,address);
        return new JsonResult<>(OK);
    }
}
```

测试：访问[http://localhost:8080/addresses/add_new_address?name=Tom&phone=178588022222](http://localhost:8080/addresses/add_new_address?name=Tom&phone=178588022222)

#### 6 新增收货地址-前端页面

```html
<script type="text/javascript">
			$("#btn-add-new-address").click(function () {
				$.ajax( {
					url:"/addresses/add_new_address",
					type:"POST",
					data:$("#form-add-new-address").serialize(),
					// data:"username="+username "&地址信息,
					dataType:"JSON",
					success:function(json){
						if(json.state === 200){
							alert("新增地址成功");

						}else{
							alert("新增地址失败");
						}
					},
					error:function(xhr){
						alert("新增地址时产生未知的错误！" + xhr.status);
					}
				} );
			});
		</script>
```

<br/>

## 获取省市区列表

#### 1 获取省市区列表-数据库

```mysql
CREATE TABLE t_dict_district (
  id int(11) NOT NULL AUTO_INCREMENT,
  parent varchar(6) DEFAULT NULL,
  code varchar(6) DEFAULT NULL,
  name varchar(16) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

parent属性表示父区域代码号，省的父代码号+86

#### 2 获取省市区列表-实体类

创建`District`实体类

```java
public class District extends BaseEntity {
    private Integer id;
    private String parent;
    private String code;
    private String name;
    
    // get和set
    // equals和hashCode
    // toString
}
```

<br/>

#### 3 获取省市区列表-持久层

查询语句，根据父代号进行查询。

```mysql
SELECT * FROM t_dict_district WHERE parent=?
ORDER BY code ASC  -- 升序
```

抽象方法定义。`DistrictMapper`接口

```
package com.cy.store.mapper;
import com.cy.store.entity.District;
import java.util.List;

public interface DistrictMapper {
    /**
     * 根据父代号查询区域信息
     * @param parent 父代号
     * @return 父区域下的所有区域列表
     */
    List<District> findByParent(Integer parent);
}
```

写DistrictMapper.xml

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!--namespace属性：用于指定当前的映射文件和哪个接口进行映射，需要指定接口的文件路径，需要标注包的完整路径接口-->
<mapper namespace="com.cy.store.mapper.DistrictMapper">
    <!--不存在区分大小写等映射问题，自定义映射规则：resultMap省了-->

    <select id="findByParent" resultType="com.cy.store.entity.District">
        SELECT * FROM t_dict_district WHERE parent=#{parent}
        ORDER BY code ASC
    </select>
</mapper>
```

单元测试

```java
package com.cy.store.mappper;

// 使用JUnit 4测试

import com.cy.store.entity.Address;
import com.cy.store.entity.District;
import com.cy.store.mapper.AddressMapper;
import com.cy.store.mapper.DistrictMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DistrictMapperTests {
    @Autowired
    private DistrictMapper districtMapper;

    @Test
    public void findByParent() {
        List<District> list = districtMapper.findByParent("210100");
        for (District d : list) {
            System.out.println(d);
        }
    }
}
```

<br/>

#### 4 获取省市区列表-业务层

1.创建接口`IDistrictService`，并定义抽象方法

```java
package com.cy.store.service;
import com.cy.store.entity.District;
import java.util.List;

public interface IDistrictService {
    /**
     * 根据父代号查询区域信息（省市区）
     * @param parent 父代号
     * @return 多个区域的信息
     */
    List<District> getByParent(String parent);
}
```

<br/>

2.创建`DistrictServiceImpl`实现类，实现抽象方法

```java
package com.cy.store.service.impl;

import com.cy.store.entity.District;
import com.cy.store.mapper.DistrictMapper;
import com.cy.store.service.IDistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistrictServiceImpl implements IDistrictService {
    @Autowired
    private DistrictMapper districtMapper;

    @Override
    public List<District> getByParent(String parent) {
        List<District> list = districtMapper.findByParent(parent);
        // 再进行网络数据传输时，为了尽量避免无效数据的传递，可以将其设置null
        for (District d : list) {
            d.setId(null);
            d.setParent(null);
        }
        return list;
    }
}
```

3.单元测试

```java
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
```

<br/>

### 4 获取省市区列表-控制层

#### 4.1 设计请求

```properties
请求路径:/districts
请求方式:GET
请求数据:String parent
响应结果:JsonResult<List<District>>
```

#### 4.2 处理请求

创建一个DistrictController类，在类中编写处理请求的方法

```java
package com.cy.store.Controller;

import com.cy.store.entity.District;
import com.cy.store.service.IDistrictService;
import com.cy.store.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("districts")
@RestController
public class DistrictController extends BaseController {
    @Autowired
    private IDistrictService districtService;

    // District开头的请求都被拦截到getByParent()方法(SpringMVC>=4.2)
    @RequestMapping({"/",""})
    public JsonResult<List<District>> getByParent(String parent){
        List<District> data = districtService.getByParent(parent);
        return new JsonResult<>(OK,data);
    }
}
```

district请求添加到拦截器白名单：`patterns.add("/district/**");`

测试：访问`localhost:8080/districts?parent=86`

<br/>

### 5 获取省市区列表-前端页面

1.注释掉addAddress.html里js获取省市区列表的代码

```html
<!--js获取省市区列表
		<script type="text/javascript" src="../js/distpicker.data.js"></script>
		<script type="text/javascript" src="../js/distpicker.js"></script>
-->
```

2.检查前端页面在提交省市区数据时是否有相关的`name`属性和`id`属性

3.运行看是否还可以正常保存数据（除了省市区之外）

<br/>

## 获取省市区的名称

### 1 获取省市区的名称-持久层

1.规划根据当前的code来获取当前省市区的名称，对应以下查询语句

```
SELECT * FROM t_dist_districts WHERE code=?
```

2.在DistrictMapper接口定义出来

```java
String findNameByCode(String code);
```

3.在DistrictMapper.xml文件中添加抽象方法映射

```java
<select id="findNameByCode" resultType="java.lang.String">
    SELECT name FROM t_dict_district WHERE code=#{code}
</select>
```

4.单元测试

```java
@Test
    public void findNameByCode(){
        String name = districtMapper.findNameByCode("610000");
        System.out.println(name);
    }
```

<br/>

### 2 获取省市区的名称-业务层

1.在业务层没有异常需要进行处理。
2.定义对应的业务层接口中的抽象方法。

```java
String getNameByCode(String code);
```

3.在子类中进行实现

```java
@Override
public String getNameByCode(String code){
    return districtMapper.findNameByCode(code);
}
```

4.测试省略（超过8行代码都要进行独立测试）

<br/>

### 3 获取省市区的名称-业务层优化

1.添加地址层依赖于`IDistrictService`层

```java
// 获取省市区的名称-业务层优化 依赖于IDistrictService的业务层接口
    @Autowired
    private IDistrictService districtService;
```

2.在addNewAddress方法中将districtService接口中获取到的省市区数据转移到address对象，这个对象中就包含了所有的用户收货地址的数据。

```java
// 对address对象中的数据进行补全：省市区
        String provinceName = districtService.getNameByCode( address.getProvinceCode() );
        String cityName = districtService.getNameByCode( address.getCityCode() );
        String areaName = districtService.getNameByCode( address.getAreaCode() );
        address.setProvinceName(provinceName);
        address.setCityName(cityName);
        address.setAreaName(areaName);
```

<br/>

### 4 获取省市区-前端页面

1.addAddress.html页面中来编写对应的省市区展示及根据用户的不同选择来限制对应的标签中的内容。

2.编写相关的事件代码。

```html
// value属性用于表示当前这个区域的code值
			let defaultOption = "<option value='0'>---- 选择区 ----</option>";
			$(document).ready(function(){
				showProvinceList();
				// 给控件二、三设置默认的“请选择”的值
				$("#city-list").append(defaultOption);
				$("#area-list").append(defaultOption);
			});


			/** 控件三：县区的下拉列表数据展示*/
			$("#city-list").change(function (){
				// 获取行政区(省)的父代码
				let parent = $("#city-list").val();
				// 清空下拉列表元素
				$("#area-list").empty();
				// 设置默认的“请选择”的值[复用]
				$("#area-list").append(defaultOption);

				if(parent == 0)return;

				$.ajax( {
					url:"/districts",
					type:"POST",
					data:"parent=" + parent,
					dataType:"JSON",
					success:function(json){
						if(json.state === 200){
							let list = json.data;
							for (let i = 0; i < list.length; i++) {
								let opt = "<option value='" + list[i].code + "'>" + list[i].name + "</option>";
								$("#area-list").append(opt);
							}
						}else{
							alert("县区信息加载失败！");
						}
					}
				} );
			});

			/** 控件二：市的下拉列表数据展示*/
			// 控件一发生改变，则：
			$("#province-list").change(function (){
				// 获取行政区(省)的父代码
				let parent = $("#province-list").val();
				// 清空下拉列表元素
				$("#city-list").empty();
				$("#area-list").empty();
				// 给控件二、三设置默认的“请选择”的值[复用]
				$("#city-list").append(defaultOption);
				$("#area-list").append(defaultOption);

				if(parent == 0)return;

				$.ajax( {
					url:"/districts",
					type:"POST",
					data:"parent=" + parent,
					dataType:"JSON",
					success:function(json){
						if(json.state === 200){
							let list = json.data;
							for (let i = 0; i < list.length; i++) {
								let opt = "<option value='" + list[i].code + "'>" + list[i].name + "</option>";
								$("#city-list").append(opt);
							}
						}else{
							alert("城市信息加载失败！");
						}
					}
				} );
			});

			/** 控件一：省的下拉列表数据展示*/
			function showProvinceList() {
				$.ajax( {
					url:"/districts",
					type:"POST",
					data:"parent=86",
					dataType:"JSON",
					success:function(json){
						if(json.state === 200){
							let list = json.data;
							for (let i = 0; i < list.length; i++) {
								let opt = "<option value='" + list[i].code + "'>" + list[i].name + "</option>";
								$("#province-list").append(opt);
							}
						}else{
							alert("省/直辖市信息加载失败！");
						}
					}
				} );
			}
```

<br/>

## 收货地址列表展示

### 1 持久层

1.数据库数据的查询操作：

```mysql
SELECT * FROM t_address WHERE uid=? 
ORDER BY is_default DESC,created_time DESC
```

2.接口和抽象方法

```java
/**
     * 根据用户id查询其收货地址数据
     * @param uid 用户id
     * @return 收货地址数据
     */
    List<Address> findByUid(Integer uid);
```

3.在xml语句中添加对应的sql语句映射

```xml
<select id="findByUid" resultMap="AddressEntityMap">
        SELECT * FROM t_address WHERE uid=#{uid}
        ORDER BY is_default DESC,created_time DESC
</select>
```

4.完成单元测试

```java
@Test
    public void findByUid(){
        List<Address> list = addressMapper.findByUid(21);
        System.out.println(list);
    }
```

⚠在此处发现AddressMapper.xml文件下的<resultMap id="AddressEntityMap" type="com.cy.store.entity.Address">写错了，且文件名写成了AdressMapper.xml

<br/>

### 2 业务层 

1.只是查询不用抛相关异常

2.设计业务层接口和抽象方法

```java
List<Address> getByUid(Integer uid);
```

3.需要在实现类中实现此方法的逻辑

```java
@Override
    public List<Address> getByUid(Integer uid) {
        List<Address> list = addressMapper.findByUid(uid);
        for (Address address : list) {
            // 过滤不需要的字段
            address.setAid(null);
            address.setUid(null);
            address.setProvinceCode(null);
            address.setCityCode(null);
            address.setAreaCode(null);
            address.setTel(null);
            address.setIsDefault(null);
            address.setCreatedTime(null);
            address.setCreatedUser(null);
            address.setModifiedTime(null);
            address.setModifiedUser(null);
        }
        return list;
    }
```

4.单元测试省略

<br/>

### 3 控制层

1.请求设计

```properties
请求路径:/addresses
请求方式:GET
请求数据:HttpSession session
响应结果:JsonResult<List<Address>>
```

2.请求方法的编写

```java
@RequestMapping({"","/"})
    public JsonResult<List<Address>> getByUid(HttpSession session) {
        Integer uid = getUidFromSession(session);
        List<Address> data = addressService.getByUid(uid);
        return new JsonResult<>(OK,data);
    }
```

3.登录，测试：http://localhost:8080/addresses

<br/>

### 4 前端页面

在address.html页面编写查询用户收货地址数据的展示列表。

```html
<script type="text/javascript">
			$(document).ready(function (){
				showAddressList();
			});
			// 在外面定义ajax方法
			function showAddressList(){
				$.ajax( {
					url:"/addresses",
					type:"GET",
					dataType:"JSON",
					success:function(json){
						if(json.state === 200){
							let list = json.data;
							for (let i = 0; i < list.length; i++) {
								// TODO
								// #{...}为占位符
								let tr = '<tr>\n' +
										'<td>#{tag}</td>\n' +
										'<td>#{name}</td>\n' +
										'<td>#{address}</td>\n' +
										'<td>#{phone}</td>\n' +
										'<td><a class="btn btn-xs btn-info"><span class="fa fa-edit"></span> 修改</a></td>\n' +
										'<td><a class="btn btn-xs add-del btn-info"><span class="fa fa-trash-o"></span> 删除</a></td>\n' +
										'<td><a class="btn btn-xs add-def btn-default">设为默认</a></td>\n' +
										'</tr>';

								// 新增：电话号码格式化（隐藏中间4位）
								let phone = list[i].phone || ''; // 处理空值
								if (phone.length >= 8) { // 至少8位才处理（前4+后4）
									phone = phone.slice(0, 4) + '***' + phone.slice(-4); // 前4位 + *** + 后4位
								} else {
									phone = phone.replace(/\d/g, '*'); // 短号码全隐藏（可选）
								}

								// 正则表达式替换，添加全局匹配标志g
								tr = tr.replace(/#{tag}/g, list[i].tag)
										.replace(/#{name}/g, list[i].name)
										.replace(/#{phone}/g, phone)
										.replace(/#{address}/g, list[i].address);

								// 追加
								$("#address-list").append(tr);
							}
							// 隐藏首个[eq(0)]“设为默认”按钮
							$(".add-def:eq(0)").hide();
						} else {
							console.error("API 错误:", json); // 打印详细错误信息
							alert("收货地址加载失败！");
						}
					}
				});
			}
		</script>
```

<br/>

<br/>

## 设置默认收货地址

### 1 持久层

#### 1.1 SQL语句规划

1.检测当前用户想设置为默认收货地址的这条数据是否存在。

```mysql
SELECT * FROM t_address WHERE aid=?
```

2.执行修改时，先将所有的收货地址设为非默认。

```mysql
UPDATE t_address SET is_default=0 WHERE uid=?
```

3.将用户当前选中的记录设为默认收货地址

```mysql
UPDATE t_address SET is_default=1,modified_user=?,modified_time=? WHERE aid=?
```

#### 1.2 设计抽象方法

在AddressMapper接口中进行定义和声明

```java
/** 设置默认收货地址 功能模块*/
    /**
     * 1.根据aid查询收货地址数据
     * @param aid 收货地址id
     * @return 收货地址数据，没有找到返回null值
     */
    Address findByAid(Integer aid);

    /**
     * 2.根据用户的uid值将其所有收货地址设为非默认
     * （演示视频的叫updateNonDefault）
     * @param uid 用户id
     * @return 受影响的行数
     */
    Integer updateNotDefault(Integer uid);

    /**
     * [核心] 将aid对应的记录设置为默认收货地址
     * @param aid
     * @param modifiedUser
     * @param modifiedTime
     * @return
     */
    Integer updateDefaultByAid(
            @Param("aid") Integer aid,
            @Param("modifiedUser") String modifiedUser,
            @Param("modifiedTime") Date modifiedTime);
```

#### 1.3 配置SQL映射

AddressMapper.xml进行配置

```xml
<select id="findByAid" resultMap="AddressEntityMap">
        SELECT * FROM t_address WHERE aid=#{aid}
    </select>

    <update id="updateNotDefault">
        UPDATE t_address SET is_default=0 WHERE uid=#{uid}
    </update>

    <update id="updateDefaultByAid">
        UPDATE t_address
        SET is_default=1,modified_user=#{modifiedUser},modified_time=#{modifiedTime}
        WHERE aid=#{aid}
    </update>
```

在单元测试方法中进行测试

```java
@Test
    public void findByAid(){
        System.out.println(addressMapper.findByAid(8));;
    }
    @Test
    public void updateNotDefault(){
        addressMapper.updateNotDefault(21);
    }
    @Test
    public void updateDefaultByAid(){
        addressMapper.updateDefaultByAid(8,"test",new Date());
    }
```

<br/>

### 2 业务层

#### 2.1 异常规划

1.在执行更新时产生位置的`UpdateException`异常，已经创建

2.访问的数据不是当前登录用户的收货地址数据，非法访问：`AccessDeniedException`异常。

3.收货地址可能不存在异常：`AddressNotFoundException`异常

#### 2.2 抽象方法

在接口`IAddressService`中编写抽象方法

```java
/**
     * 修改某个用户的某条收货地址数据为默认收货地址
     * @param aid 收货地址id
     * @param uid 用户id
     * @param username 修改人
     */
    void setDefault(Integer aid,Integer uid,String username);
```

#### 2.3 实现抽象方法

在`AddressServiceImpl`类中进行开发和业务设计。

```java
@Override
    public void setDefault(Integer aid, Integer uid, String username) {
        Address result = addressMapper.findByAid(aid);
        if (result == null) {
            throw new AddressNotFoundException("收货地址不存在");
        }
        // 检测当前获取到的收货地址数据的归属
        if ( !result.getUid().equals(uid) ) {
            throw new AccessDeniedException("非法访问！");
        }
        // 先将所有的收货地址设置非默认
        Integer rows = addressMapper.updateNotDefault(uid);
        if (rows < 1) {
            throw new UpdateException("更新数据时产生未知的异常");
        }
        // 将用户选中某条地址设置为默认收货地址
        rows = addressMapper.updateDefaultByAid(aid,username,new Date());
        if (rows != 1) {
            throw new UpdateException("更新数据时产生未知的异常");
        }
    }
```

#### 2.4 单元测试

```java
@Test
    public void setDefault(){
        addressService.setDefault( 8,21,"admin" );
    }
```

<br/>

### 3 控制层

#### 3.1 处理异常

在`BaseController`类中进行统一的处理。

```java
else if(e instanceof AddressNotFoundException){
      result.setState(4004);
      result.setMessage("收货地址不存在的异常");
}
else if(e instanceof AccessDeniedException){
      result.setState(4005);
      result.setMessage("收货地址数据非法访问的异常");
}
```

#### 3.2 设计请求

`@PathVariable`是[请求路径]与参数值不一致时，作强行匹配之用

```properties
请求路径:/addresses/{aid}/set_default
请求方式:GET
请求数据:@PathVariable("aid") Integer aid,HttpSession session
响应结果:JsonResult<Void>
```

#### 3.3 完成请求方法

在`AddressController`类中编写请求处理方法。

```java
// RestFul风格的请求编写
    @RequestMapping("{aid}/set_default")
    public JsonResult<Void> setDefault(@PathVariable("aid") Integer aid,
                                       HttpSession session){
        addressService.setDefault(
                aid,
                getUidFromSession(session),
                getUsernameFromSession(session) );
        return new JsonResult<>(OK);
```

访问测试：http://localhost:8080/addresses/8/set_default，看数据库中对应的记录的默认值是否变为1，且其他都为0

<br/>

### 4 前端页面

1.1.给设置默认收货地址按钮添加一个onlick属性，指向一个方法的调用，在这个方法中来完成ajax请求的方法。

```javascript
// #{...}为占位符
								let tr = '<tr>\n' +
										'<td>#{tag}</td>\n' +
										'<td>#{name}</td>\n' +
										'<td>#{address}</td>\n' +
										'<td>#{phone}</td>\n' +
										'<td><a class="btn btn-xs btn-info"><span class="fa fa-edit"></span> 修改</a></td>\n' +
										'<td><a class="btn btn-xs add-del btn-info"><span class="fa fa-trash-o"></span> 删除</a></td>\n' +
										'<td><a onclick="setDefault(#{aid})" class="btn btn-xs add-def btn-default">设为默认</a></td>\n' +
										'</tr>';
// 正则表达式替换，添加全局匹配标志g
tr = tr.replace(/#{tag}/g, list[i].tag)
		.replace(/#{name}/g, list[i].na
		.replace(/#{phone}/g, phone)
		.replace(/#{address}/g, list[i]
tr = tr.replace("#{aid}",list[i].aid);
```

2.address.html页面点击"设置默认"按钮，来发送ajax请求。完成setDefault()方法的声明

```javascript
function setDefault(aid) {
				$.ajax( {
					url:"/addresses/" + aid + "/set_default",
					type:"GET",
					dataType:"JSON",
					success:function(json){
						if(json.state == 200){
							// 重载收货地址列表
							showAddressList();
							alert("默认收货地址设置成功");
						}else{
							alert("默认收货地址设置失败");
							// console.log(json.state);
						}
					},
					error:function(xhr){
						alert("设置默认收货地址时产生未知的异常：" + xhr.message);
					}
				} );
```

测试：访问address.html

<br/>

## 删除收货地址

### 1 持久层

#### 1.1规划需要执行的SQL语句

1.在删除之前判断该数据是否存在，判断该条地址数据的归属是否是当前的用户。不用重复开发。

2.执行删除

```mysql
DELETE FROM t_address WHERE aid=?
```

3.如果用户删除的是默认收货地址，将剩下的地址中的某一条设置为默认的收货地址。规则可以自定义：最新修改的收货地址设置为默认的收货地址(`modified_time`的字段值)。

```mysql
SELECT * FROM t_address WHERE uid=?
ORDER BY modified_time DESC limit 0,1
-- limit [起始偏移量,] 行数
```

4.如果用户本身只有一条收货地址，将其删除后，其他操作无需进行

#### 1.2 设计抽象方法

在AddressMapper接口中进行抽象方法的设计

```java
/**
     * 根据收货地址id删除收货地址数据
     * @param aid 收货地址id
     * @return 受影响的行数
     */
    Integer deleteByAid(Integer aid);

    /**
     * 根据用户id查询当前用户最后一次被修改的收货地址数据
     * @param uid 用户id
     * @return 收货地址数据
     */
    Address findLastModified(Integer uid);
```

#### 1.3 映射SQL语句

在AddressMappper.xml文件中完成映射。

```xml
<delete id="deleteByAid">
    DELETE FROM t_address WHERE aid=#{aid}
</delete>
<select id="findLastModified" resultMap="AddressEntityMap">
    SELECT * FROM t_address WHERE uid=#{uid}
    ORDER BY modified_time DESC limit 0,1
</select>
```

#### 1.4 单元测试

```java
// 删除默认收货地址 单元测试
    @Test
    public void deleteByAid() {
        addressMapper.deleteByAid(5);
    }
    @Test
    public void findLastModified() {
        System.out.println( addressMapper.findLastModified(21) );
    }
```

<br/>

### 2 业务层

#### 2.1 规划异常

在执行删除的时候可能产生未知的异常，抛出DeleteException

```java
/** 删除数据时产生的异常 */
public class DeleteException extends ServiceException {
  // ...
}
```

#### 2.2 抽象方法设计

在IAddressService接口中设计抽象方法。根据前面的设计，调用的方法不少，但参数也就3个。

```java
/**
     * 删除用户选中的收货地址数据
     * @param aid 收货地址id
     * @param uid 用户id
     * @param username 用户名
     */
    void delete(Integer aid,Integer uid,String username);
```

#### 2.3 实现抽象方法

业务层方法设计和实现

```java
@Override
    public void delete(Integer aid, Integer uid, String username) {
        Address result = addressMapper.findByAid(aid);
        if (result == null) {
            throw new AddressNotFoundException("收货地址不存在");
        }
        // 复用
        if ( !result.getUid().equals(uid) ) {
            throw new AccessDeniedException("非法访问！");
        }

        // 不论是否默认地址都要删
        Integer rows = addressMapper.deleteByAid(aid);
        if (rows != 1) {
            throw new DeleteException("删除数据时产生未知的异常");
        }

        Integer count = addressMapper.countByUid(uid);
        if (count == 0) {
            // 终止程序
            return;
        }

        // 判断：删除的记录是否默认地址
        if (result.getIsDefault() == 0) {
            return;     //否则终止程序
        }

        // 被删除的记录是默认地址：
        // 将最新修改的收货地址设置为默认的收货地址
        // 将这条数据中的is_default字段的值设置为1
        Address address = addressMapper.findLastModified(uid);
        rows = addressMapper.updateDefaultByAid(
                address.getAid(),username,new Date()
        );
        if (rows != 1) {
            throw new UpdateException("更新数据时产生未知的异常");
        }
    }
```

#### 2.4 单元测试

```java
@Test
    public void delete(){
        addressService.delete( 9,21,"test" );
    }
```

<br/>

#### 3 控制层

1.需要在`BaseController`中添加异常`DeleteException`

```java
else if(e instanceof DeleteException){
    result.setState(5002);
    result.setMessage("删除数据时产生未知的异常");
}
```

2.设计请求处理

```properties
请求路径:/addresses/{aid}/delete
请求方式:POST
请求数据:Integer aid,HttpSession session
响应结果:JsonResult<Void>
```

3.编写请求处理方法实现

```java
@RequestMapping("{aid}/delete")
    public JsonResult<Void> delete(@PathVariable("aid") Integer aid, HttpSession session) {
        addressService.delete(
                aid,
                getUidFromSession(session),
                getUsernameFromSession(session) );
        return new JsonResult<>(OK);
    }
```

<br/>

#### 4 前端页面

在address.html添加删除按钮的事件

```html
<a onclick="deleteByAid(#{aid})" class="btn btn-xs add-del btn-info"><span class="fa fa-trash-o"></span> 删除</a>
```

编写`deleteByAid(#{aid})`方法的具体实现

```javascript
function deleteByAid(aid) {
				$.ajax( {
					url:"/addresses/" + aid + "/delete",
					type:"POST",
					dataType:"JSON",
					success:function(json){
						if(json.state == 200){
							// 重载收货地址列表
							showAddressList();
							alert("收货地址已删除");
						}else{
							alert("删除收货地址失败");
							// console.log(json.state);
						}
					},
					error:function(xhr){
						alert("删除收货地址时产生未知的异常：" + xhr.message);
					}
				} );
			}
```

测试：http://localhost:8080/web/address.html

经测试，此时两个onclick里面aid一致，“设为默认”功能失效。我直接改成setDefault(#{aid2})，再在后面添加一条正则替换就可以解决。

<br/>

==修改用户收货地址留待以后完成==