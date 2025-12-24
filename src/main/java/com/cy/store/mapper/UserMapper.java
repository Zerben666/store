package com.cy.store.mapper;
import com.cy.store.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/** 用户模块的持久层接口 */
//@Mapper
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
     * @return 如果找到对应的用户则返回其数据，反之返回null值
     */
    User findByUsername(String username);

    /**
     * 根据用户的uid修改用户密码
     * @param uid 用户的id
     * @param password 用户输入的新密码
     * @param modifiedUser 修改的执行者
     * @param modifiedTime 修改数据的时间
     * @return 受影响的行数
     */
    Integer updatePasswordByUid(
            @Param("uid") Integer uid,
            @Param("password") String password,
            @Param("modifiedUser") String modifiedUser,
            @Param("modifiedTime") Date modifiedTime
    );

    /**
     * 根据用户id来查询用户的数据
     * @param uid 用户的id
     * @return 如果找到对应的用户则返回其数据，反之返回null值
     */
    User findByUid(Integer uid);

    /**
     * 更新用户的数据信息
     * @param user 用户的数据（电话号码、电子邮箱、性别三合一）
     * @return 受影响的行数
     */
    Integer updateInfoByUid(User user);

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
}
