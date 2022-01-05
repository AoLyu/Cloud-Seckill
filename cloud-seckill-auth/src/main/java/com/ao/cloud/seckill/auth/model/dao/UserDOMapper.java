package com.ao.cloud.seckill.auth.model.dao;


import com.ao.cloud.seckill.auth.model.dataobject.UserDO;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDOMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(UserDO record);

    int insertSelective(UserDO record);

    UserDO selectByPrimaryKey(Integer id);

    UserDO selectByTelphone(String telphone);

    int updateByPrimaryKeySelective(UserDO record);

    int updateByPrimaryKey(UserDO record);
}
