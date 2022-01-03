package com.ao.cloud.seckill.item.model.dao;


import com.ao.cloud.seckill.item.model.dataobject.StockLogDO;
import org.springframework.stereotype.Repository;

@Repository
public interface StockLogDOMapper {

    int deleteByPrimaryKey(String stockLogId);

    int insert(StockLogDO record);

    int insertSelective(StockLogDO record);

    StockLogDO selectByPrimaryKey(String stockLogId);

    int updateByPrimaryKeySelective(StockLogDO record);

    int updateByPrimaryKey(StockLogDO record);
}
