package com.ao.cloud.seckill.order.model.dao;


import com.ao.cloud.seckill.order.model.dataobject.OrderDO;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDOMapper {

    int deleteByPrimaryKey(String id);

    int insert(OrderDO record);

    int insertSelective(OrderDO record);

    OrderDO selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(OrderDO record);

    int updateByPrimaryKey(OrderDO record);
}
