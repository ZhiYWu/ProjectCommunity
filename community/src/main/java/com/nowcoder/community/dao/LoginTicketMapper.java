package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {

    /**
     *  登录成功要插入一个凭证
     * @param loginTicket
     * @return
     */
    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    // 配置文件中写的主键自增只能作用在mapper.xml文件中，注解时需要@Options才能生效
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);


    /**
     * ticket是核心凭证，利用ticket查询是哪个用户在进行登录等操作
     * @param ticket
     * @return
     */
    @Select({
            "select id, user_id, ticket, status, expired from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);



    /**
     * 登出即修改状态
     * @param ticket
     * @param status
     * @return
     */
    // @Update 注解中的script标签的写法是演示动态SQL怎么进行书写，在此处不发挥作用
    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket}",
            "<if test=\"ticket!=null\">",
            "and 1=1",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);


}
