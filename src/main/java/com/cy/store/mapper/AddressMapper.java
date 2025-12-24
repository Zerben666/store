package com.cy.store.mapper;

import com.cy.store.entity.Address;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

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

    /**
     * 根据用户id查询其收货地址数据
     * @param uid 用户id
     * @return 收货地址数据
     */
    List<Address> findByUid(Integer uid);

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
}
