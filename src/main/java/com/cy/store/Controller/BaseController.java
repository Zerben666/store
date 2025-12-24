package com.cy.store.Controller;

import com.cy.store.Controller.ex.FileEmptyException;
import com.cy.store.Controller.ex.FileSizeException;
import com.cy.store.Controller.ex.FileStateException;
import com.cy.store.Controller.ex.FileTypeException;
import com.cy.store.service.IUserService;
import com.cy.store.service.ex.*;
import com.cy.store.util.JsonResult;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.impl.FileUploadIOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

/** 表示控制层类的基类*/
public class BaseController {
    /** 操作成功的状态码*/
    public static final int OK = 200;

    // 请求处理方法：返回值需要传递给前端的数据
    // 自动将异常对象传递给此方法的参数列表上
    // 当前项目中产生了异常，被统一拦截到此方法中。这个方法此时就充当请求处理方法，方法的返回值直接给到前端
    @ExceptionHandler({ServiceException.class, FileUploadException.class}) // 用于统一处理抛出的异常
    public JsonResult<Void> handleException(Throwable e){
        JsonResult<Void> result = new JsonResult<>(e);
        // 精确的异常从4000开始
        if(e instanceof UsernameDuplicatedException){
            result.setState(4000);
            result.setMessage("用户名被占用");
        }
        else if(e instanceof UsernameNotFoundException){
            result.setState(4001);
            result.setMessage("用户数据不存在异常");
        }
        else if(e instanceof PasswordNotMatchException){
            result.setState(4002);
            result.setMessage("用户名或密码错误");
        }
        else if(e instanceof AddressCountLimitException){
            result.setState(4003);
            result.setMessage("收货地址超出上限的异常");
        }
        else if(e instanceof AddressNotFoundException){
            result.setState(4004);
            result.setMessage("收货地址不存在的异常");
        }
        else if(e instanceof AccessDeniedException){
            result.setState(4005);
            result.setMessage("收货地址数据非法访问的异常");
        }
        else if(e instanceof ProductNotFoundException){
            result.setState(4006);
            result.setMessage("商品数据不存在的异常");
        }
        else if(e instanceof CartNotFoundException){
            result.setState(4007);
            result.setMessage("购物车数据不存在的异常");
        }
        // 笼统的异常从5000开始
        else if(e instanceof InsertException){
            result.setState(5000);
            result.setMessage("注册时产生了未知的异常");
        }
        else if(e instanceof UpdateException){
            result.setState(5001);
            result.setMessage("更新数据时产生未知的异常");
        }
        else if(e instanceof DeleteException){
            result.setState(5002);
            result.setMessage("删除数据时产生未知的异常");
        }
        // 文件上传异常
        else if (e instanceof FileEmptyException) {
            result.setState(6000);
            result.setMessage("文件为空异常");
        }
        else if (e instanceof FileSizeException) {
            result.setState(6001);
            result.setMessage("文件大小异常");
        }
        else if (e instanceof FileTypeException) {
            result.setState(6002);
            result.setMessage("文件类型异常");
        }
        else if (e instanceof FileStateException) {
            result.setState(6003);
            result.setMessage("文件状态异常");
        }
        else if (e instanceof FileUploadIOException) {
            result.setState(6004);
            result.setMessage("文件读写异常");
        }
        return  result;
    }

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

    @Autowired
    private IUserService userService;   // 演示视频用的是userService，但好像没有那啥
    @RequestMapping("change_password")
    public JsonResult<Void> changePassword(String oldPassword,
                                           String newPassword,
                                           HttpSession session){
        Integer uid = getUidFromSession(session);
        String username = getUsernameFromSession(session);
        userService.changePassword(uid,username,oldPassword,newPassword);
        return new JsonResult<>(OK);
    }
}
