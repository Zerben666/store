package com.cy.store.service.impl;

import com.cy.store.entity.User;
import com.cy.store.mapper.UserMapper;
import com.cy.store.service.IUserService;
import com.cy.store.service.ex.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.Objects;
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

    @Override
    public void changePassword(Integer uid, String username, String oldPassword, String newPassword) {
        User result = userMapper.findByUid(uid);
        if (result == null || result.getIsDelete() ==1) {
            throw new UsernameNotFoundException("用户数据不存在");
        }
        // 用户输入密码和数据库中密码进行比较
        String oldMd5Password = getMD5Password(oldPassword, result.getSalt());
        /*String newMd5Password = getMD5Password(newPassword, result.getSalt());
        if(!Objects.equals(oldMd5Password, newMd5Password)){
            throw new PasswordNotMatchException("密码错误");
        }*/
        if ( !result.getPassword().equals(oldMd5Password) ) {
            throw new PasswordNotMatchException("密码错误");
        }

        // 更新数据库密码
        String newMd5Password = getMD5Password(newPassword, result.getSalt());
        Integer rows = userMapper.updatePasswordByUid(
                                uid,newMd5Password,
                                username,new Date() );
        if(rows != 1){
            throw new UpdateException("更新数据时产生未知的异常");
        }
    }

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
        if (rows != 1) {
            throw new UpdateException("更新数据时产生未知的异常");
        }
    }

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

    /** 定义一个md5算法的加密处理 */
    private String getMD5Password(String password,String salt){
        // md5加密算法调用 ×3
        for (int i =0; i < 3; i++){
            password = DigestUtils.md5DigestAsHex( (salt+password+salt).getBytes() ).toUpperCase();
        }
        return password;
    }
}
