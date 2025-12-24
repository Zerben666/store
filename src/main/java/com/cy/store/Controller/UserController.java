package com.cy.store.Controller;

import com.cy.store.Controller.ex.FileEmptyException;
import com.cy.store.Controller.ex.FileSizeException;
import com.cy.store.Controller.ex.FileStateException;
import com.cy.store.Controller.ex.FileUploadIOException;
import com.cy.store.entity.User;
import com.cy.store.service.IUserService;
import com.cy.store.service.ex.InsertException;
import com.cy.store.service.ex.UsernameDuplicatedException;
import com.cy.store.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// @Controller
@RestController // @Controller + @ResponseBody
@RequestMapping("users")
public class UserController extends BaseController {
    @Autowired
    private IUserService userService;

    /**
     * 约定大于配置开发思想：省略大量的配置甚至注解的编写
     *
     * 1.接收数据方式：请求处理方法的参数列表设置为pojo类型来接收前端的数据，
     *  SpringBoot会将前端url地址中的参数名和pojo类的属性名进行比较，
     *  如果这两个名称相同，则将值注入到pojo类中对应的属性上
     */
    // 处理注册请求
    @RequestMapping("reg")
    // @ResponseBody // 表示此方法的响应结果以json格式进行数据的响应给到前端
    public JsonResult<Void> reg(User user){
        userService.reg(user);
        return new JsonResult<>(OK);
    }

    /*
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
    }*/

    // 测试：http://localhost:8080/users/reg?username=user0001&password=123456

    /** 2.接收数据方式：请求处理方法的参数列表设置为非pojo类型，
     * SpringBoot会直接将请求的参数名和方法的参数名进行比较，
     * 如果名称相同则自动完成值的依赖注入
     */
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


    // 上传头像的最大值：<=10M（SpringMVC默认单位字节）
    public static final int AVATAR_MAX_SIZE = 10 * 1024 * 1024;
    /** 上传文件的类型限制 */
    public static final List<String> AVATAR_TYPE = new ArrayList<>();
    // 初始化集合：静态块
    static {
        AVATAR_TYPE.add("image/jpeg");
        AVATAR_TYPE.add("image/png");
        AVATAR_TYPE.add("image/bmp");
        AVATAR_TYPE.add("image/gif");
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
        userService.changeAvatar(uid,avatar,username);
        // 返回用户头像的路径给前端页面，将来用于头像展示使用
        return new JsonResult<>(OK,avatar);
    }
}
