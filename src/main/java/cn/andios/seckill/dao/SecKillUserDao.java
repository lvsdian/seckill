package cn.andios.seckill.dao;

import cn.andios.seckill.domain.SecKillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/15/16:17
 */
@Mapper
@Repository
public interface SecKillUserDao {
    /**
     * 根据id查找user
     * @param id
     * @return
     */
    @Select("select * from seckill_user where id = #{id}")
    public SecKillUser getById(@Param("id") Long id);

    /**
     * 更新密码
     * @param newSecKillUser
     */
    @Update("update seckill_user set password =  #{password} where id = #{id}")
    void updatePassword(SecKillUser newSecKillUser);
}
