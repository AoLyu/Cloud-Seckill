package com.ao.cloud.seckill.auth.model.dao;

import com.ao.cloud.seckill.auth.model.dataobject.UserPasswordDO;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPasswordDOMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(UserPasswordDO record);

    int insertSelective(UserPasswordDO record);

    UserPasswordDO selectByPrimaryKey(Integer id);

    UserPasswordDO selectByUserId(Integer userId);

    int updateByPrimaryKeySelective(UserPasswordDO record);


    int updateByPrimaryKey(UserPasswordDO record);
}
