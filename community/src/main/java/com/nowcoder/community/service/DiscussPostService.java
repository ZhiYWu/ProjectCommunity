package com.nowcoder.community.service;

import com.alibaba.fastjson.JSONObject;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.RedisKeyUtil;
import com.nowcoder.community.util.SensitiveFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    @Value("${spring.redis.expire-seconds}")
    private int redisExpireSeconds;

    @Autowired
    private RedisTemplate redisTemplate;

    // Caffeine核心接口: Cache, LoadingCache(同步, 一般用这个), AsyncLoadingCache(异步)
    // 帖子列表的缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数的缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    // 缓存只需要初始化一次
    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    // 如果缓存中没有数据应该怎么查数据
                    @Override
                    public List<DiscussPost> load(String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 添加二级缓存: Caffeine -> Redis(二级缓存) -> mysql
                        // redisKey: popular:posts:offset:limit
                        String redisKey = RedisKeyUtil.getPopularPostsKey() + ":" + offset + ":" + limit;
                        Object postsObject = redisTemplate.opsForValue().get(redisKey);
                        // 如果Redis中包含数据则直接使用
                        if (postsObject != null) {
                            logger.debug("load post list from Redis.");
                            String postsStr = postsObject.toString();
                            return JSONObject.parseArray(postsStr, DiscussPost.class);
                        }

                        logger.debug("load post list from DB.");
                        List<DiscussPost> posts = discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                        // 将DB中查询到的数据存入Redis中,方便下次直接调用
                        redisTemplate.opsForValue().set(redisKey, JSONObject.toJSON(posts));
                        // 设置过期时间
                        redisTemplate.expire(redisKey, redisExpireSeconds, TimeUnit.SECONDS);
                        return posts;
                    }
                });

        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public Integer load(Integer key) throws Exception {
                        // 添加查询帖子条目数的二级缓存
                        String redisKey = RedisKeyUtil.getRowsKey() + ":" + key;
                        Object rowsObject = redisTemplate.opsForValue().get(redisKey);
                        if (rowsObject != null) {
                            logger.debug("load rows from Redis.");
                            String rowsStr = rowsObject.toString();
                            return Integer.valueOf(rowsStr);
                        }

                        logger.debug("load post rows from DB.");
                        int rows = discussPostMapper.selectDiscussPostRows(key);
                        // 将DB中查询到的数据存入Redis中,方便下次直接调用
                        redisTemplate.opsForValue().set(redisKey, rows);
                        // 设置过期时间
                        redisTemplate.expire(redisKey, redisExpireSeconds, TimeUnit.SECONDS);
                        return rows;
                    }
                });
    }

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        // 只缓存热门帖子
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    public int findDiscussPostRows(int userId) {
        if (userId == 0) {
            return postRowsCache.get(userId);
        }
        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }


    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        // HtmlUtils.htmlEscape 用于转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }


    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
