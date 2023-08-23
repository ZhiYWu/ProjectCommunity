package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    /**
     * 查询返回所有符合条件的帖子
     * @param userId 为了实现查询用户自己的所有帖子
     * @param offset 每一页起始行的行号
     * @param limit 每一页最多显示多少条数据
     * @return 所有符合条件的帖子
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    /**
     *
     * @param userId @param用于给参数起别名，并且如果只有一个参数，在<if>标签中使用，就必须起别名
     * @return
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

}
