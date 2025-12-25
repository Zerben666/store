## 上传头像

### 1 上传头像-持久层

#### 1.1 SQL语句的规划

将对象文件保存在服务器，在数据库中记录文件路径。

```mysql
UPDATE t_user SET avatar=?,modified_user=?,modified_time=?
WHERE uid=?
```

#### 1.2 设计接口和抽象方法

UserMapper接口中定义个抽象方法用于修改用户的头像。

```java
/**
     * 修改用户的头像
     * @param uid uid
     * @param avatar 头像路径
     * @param modifiedUser 修改者
     * @param modifiedTime 修改时间
     * @return
     */
    //
    // @Param:别名注入这个占位符，相同可以省略
    /** Param("SQL映射文件中#{}占位符的变量名“)：解决的问题:
     * 当SQL语句的占位简和映射的接口方法参数名不一致时，
     * 需要将某个参数强行注入到某个占位符变量上时，用来标注映射关系
     */
    Integer updateAvatarByUid(@Param("uid") Integer uid,
                              @Param("avatar") String avatar,
                              @Param("modifiedUser") String modifiedUser,
                              @Param("modifiedTime") Date modifiedTime);
```

#### 1.3 接口的映射

UserMapper.xml文件中编写映射的SQL语句。

```mysql
<update id="updateAvatarByUid">
        UPDATE t_user SET
            avatar = #{avatar},
            modified_user=#{modifiedUser},
            modified_time=#{modifiedTime}
        WHERE uid = #{uid}
    </update>
```

在测试类中编写测试的方法

```java
    @Test
    public void updateAvatarByUid(){
        userMapper.updateAvatarByUid(
                21,"/upload/avatar.png",
                "管理员",new Date() );
```

<br/>

### 2 上传头像-业务层

#### 2.1 规划异常

1.找不到用户数据

2.更新数据时产生未知的异常

> 无需重复开发

#### 2.2 设计接口和抽象方法

```java
/**
     * 修改用户的头像
     * @param uid 用户id
     * @param avatar 用户头像
     * @param username 用户名（修改人）
     */
    void changeAvatat(Integer uid,String avatar,String username);
```

#### 2.3 实现抽象方法

（UserServiceImpl划到上面实现方法）

编写业务层的更新用户头像的方法

```java
@Override
    public void changeAvatar(Integer uid, String avatar, String username) {
        // 查询当前的用户数据是否存在
        User result = userMapper.findByUid(uid);
        if (result == null || result.getIsDelete()==1) {
            throw new UsernameNotFoundException("用户数据不存在");
        }
        Integer rows = userMapper.updateAvatarByUid(uid,avatar,username,new Date());
        if (rows != 1) {
            throw new UpdateException("更新时产生未知的异常");
        }
    }
```

测试业务层方法执行

```java
@Test
    public void changeAvatar(){
        userService.changeAvatar( 21,"/upload/test.png","小明" );
    }
```

<br/>

### 3 上传头像-控制层

#### 3.1 规划异常

```
文件异常的父类：
  FileUploadException 泛指文件上传的异常（父类） 继承RuntimeException

文件异常的子类：
  FileEmptyException 文件为空异常
  FileSizeException 文件大小异常
  FileTypeException 文件类型异常
  FileUploadIOException 文件读写异常
  FileStateException 上传的文件状态异常
```

> 五个构造方法显示的声明出来，再去继承相关的父类

#### 3.2 处理异常

在基类BaseContoller类中进行编写和统一处理。

```java
// 文件上传异常
        else if (e instanceof FileEmptyException) {
            result.setState(6000);
        }
        else if (e instanceof FileSizeException) {
            result.setState(6001);
        }
        else if (e instanceof FileTypeException) {
            result.setState(6002);
        }
        else if (e instanceof FileStateException) {
            result.setState(6003);
        }
        else if (e instanceof FileUploadIOException) {
            result.setState(6004);
        }
```

在异常统一处理方法的参数列表上增加新的异常处理作为它的参数。

```java
@ExceptionHandler({ServiceException.class, FileUploadException.class}) // 用于统一处理抛出的异常
```

#### 3.3 设计请求

```properties
请求路径:/users/change_avatar
请求方式:POST （GET请求提交数据限制2KB）
请求数据:HttpSession session,MultipartFile file
响应结果:JsonResult<String>
```

#### 实现请求

```java
// 上传头像的最大值：<=10M（SpringMVC默认单位字节）
    public static final int AVATAR_MAX_SIZE = 10 * 1024 * 1024;
    /** 上传文件的类型限制 */
    public static final List<String> AVATAR_TYPE = new ArrayList<>();
    // 初始化集合：静态块
    static {
        AVATAR_TYPE.add("images/jpeg");
        AVATAR_TYPE.add("images/png");
        AVATAR_TYPE.add("images/bmp");
        AVATAR_TYPE.add("images/gif");
    }

    /**
     * MultipartFile 接口是SpringMVC提供的一个接口，
     * 这个接口为我们包装了获取文件类型的数据（任何类型的file都可以接收）。
     *
     * SpringBoot整合了SpringMVC，只需要在处理清求的方法参数列表上声明一个参数类型为
     * MultipartFile的参数，然后SpringBoot自动将传递给服务的文件数据赋值赋值给这个参数。
     *
     * 
     * @param session
     * @param file
     * @return
     */
    @RequestMapping("change_avatar")
    public JsonResult<String> changeAvatar(
            HttpSession session,
            // 若是前后端不一致，可用@RequestParam()
            @RequestParam("file") MultipartFile file){
        // 判断文件是否为null
        if ( file.isEmpty() ) {
            throw new FileEmptyException("上传文件为空");
        }
        // 判断文件大小
        if (file.getSize() > AVATAR_MAX_SIZE) {
            throw new FileSizeException("文件大小超出限制");
        }
        /* 判断文件类型 */
        String contentType = file.getContentType();
        // AVATAR_TYPE.contains()：如果集合包含某个元素则返回true
        if ( !AVATAR_TYPE.contains(contentType) ) {
            throw new FileSizeException("文件类型不支持");
        }

        // 上传的文件目录结构：/upload/file.png
        String parent =
                session.getServletContext().
                        getRealPath( "upload" );
        // File对象指向这个路径，File是否存在
        File dir = new File(parent);
        if ( !dir.exists() ) {
            dir.mkdirs(); // 创建当前的目录
        }
        // 获取文件名，UUID工具生成新字符串作为文件名
        String originalFilename = file.getOriginalFilename();
        System.out.println("OriginalFilename = " + originalFilename);
        // 从获取的文件名提取后缀
        int index = originalFilename.lastIndexOf(".");
        String suffix = originalFilename.substring(index);
        // 形如AHSDI1-23QIE-WBSUW-GSYWEU-8297AS-HDUDG2.png
        String filename =
                UUID.randomUUID().toString().toUpperCase()
                + suffix;
        // 以UUID生成的文件名创建空的文件
        File dest = new File(dir,filename);
        // 将file数据写入空文件(前提文件后缀一致）
        try{
            file.transferTo(dest);
        }catch(FileStateException e){
            throw new FileStateException("文件状态异常");
        }catch (IOException e){
            throw new FileUploadIOException("文件读写异常");
        }

        Integer uid = getUidFromSession(session);
        String username = getUsernameFromSession(session);
        // 返回头像的相对路径 /upload/test.png
        String avatar = "/upload/" + filename;
        userService.changeAvatat(uid,avatar,username);
        // 返回用户头像的路径给前端页面，将来用于头像展示使用
        return new JsonResult<>(OK,avatar);
    }
```

<br/>

### 4 上传头像-前端页面

在upload页面中编写上传头像的代码。

> 说明：如果直接使用表单进行文件的上传，需要给表单添加属性`enctype="multipart/form-data"`，如此不会像默认的enctype`application/x-www-form-urlencoded`将目标文件的数据结构做修改在上传，不同于字符串。

```html
<!--上传头像表单开始-->
<form action="/users/change_avatar" method="post" enctype="multipart/form-data" class="form-horizontal" role="form">
```

<br/>

### 5 解决Bug

#### 5.1 更改默认的大小限制

> 那个10MB 是你自己设置的最大限制 作为判断用的 但是springboot文件传输大小没有设置

SpringMVC默认为1MB文件可以进行上传，手动的去修改SpringMVC默认上传文件的大小。

方式1：直接可以在配置文件中进行配置：

```
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=15MB
```

方式2：采用Java代码，主类中进行配置，可以定义一个方法，必须使用`@Bean`来修饰。在类的前面添加`@Configuration`注解修改类让其生效。

```java
@Configuration  // 配置类
@SpringBootApplication
// MapperScan注解：指定当前项目中的Mapper接口路径的位置,项目启动时自动加载接口文件
@MapperScan("com.cy.store.mapper")
public class StoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreApplication.class, args);
    }

    @Bean
    public MultipartConfigElement getMultipartConfigElement(){
        // 创建一个配置的工厂类对象
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // 设置需要创建的对象的相关信息
        factory.setMaxFileSize( DataSize.of(10, DataUnit.MEGABYTES) );
        factory.setMaxRequestSize( DataSize.of(15, DataUnit.MEGABYTES) );

        // 通过工厂类创建MultipartConfigElement对象
        return factory.createMultipartConfig();
    }
}
```

#### 5.2 显示头像

在页面中通过ajax请求来提交文件，提交完成后返回了json串，解析出data中数据，设置到img头像标签的src属性上就可以了。

- .serialize()：可以将表单数据数据自动拼接成key=value的结构进行提交给服务器，一般提交是普通的控件类型中的数据(text\password\radio\checkbox)等等
- F==or==mData类：将表单中数据保持原有的结构进行数据的提交。`new FromData( $("#form")[0] );`
  ```javascript
  new FormData( $("#form")[0] );  // 文件类型的数据可以使用FormData对象进行存储
  ```

- ajax默认处理数据按照字符串形式进行，以及默认会采用字符串的形式提交数据。关闭这两个默认的功能
  ```javascript
  processData:false,	// 处理数据的形式（关闭默认）
  contentType:false,	// 提交数据的形式
  ```

<br/>

#### 5.3 登录后显示头像

可以在更新头像成功后，将服务器返回的头像路径保存在客户端的cookie对象，然后每次检测到用户打开upload.html，在页面中通过`ready()`方法来自动检测cookie中的头像路径并设置到src属性上。

1.设置cookie中的值

导入cookie.js文件

```html
<script src="../bootstrap3/js/jquery.cookie.js" type="text/javascript" charset="utf-8"></script>
```

调用cookie方法：

```javascript
$.cookie(key,value,time); // 存活时间单位：天
```

2.在upload.html页面引入cookie.js文件

3.在upload.html页面通过ready()自动读取cookie中的数据

```javascript
$(document).ready(function() {
				// 获取cookie值设置到头像的src属性上
				let avatar = $.cookie("avatar");
				console.log(avatar);
				$("#img-avatar").attr("src",avatar);
			});
```

#### 5.4 显示最新的头像

在更改完头像后，将最新的头像地址，再次保存到cookie,同名保存会覆盖有cookie中值。

```javascript
$.cookie("avatar",json.data,{expires:7});
```