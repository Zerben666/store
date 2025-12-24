package com.cy.store.service.impl;

import com.cy.store.entity.Cart;
import com.cy.store.entity.Product;
import com.cy.store.mapper.CartMapper;
import com.cy.store.mapper.ProductMapper;
import com.cy.store.service.ICartService;
import com.cy.store.service.ex.AccessDeniedException;
import com.cy.store.service.ex.CartNotFoundException;
import com.cy.store.service.ex.InsertException;
import com.cy.store.service.ex.UpdateException;
import com.cy.store.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
            // 进行原有数据和现有传递的数据相加操作（前端、后端业务层皆可）
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

    @Override
    public List<CartVO> getVOByUid(Integer uid) {
        return cartMapper.findVOByUid(uid);
    }

    @Override
    public Integer addNum(Integer cid, Integer uid, String username) {
        Cart result = cartMapper.findByCid(cid);
        if (result == null) {
            throw new CartNotFoundException("数据不存在");
        }
        // 修正：验证当前用户的 UID 是否与购物车记录的 UID 一致
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
}
