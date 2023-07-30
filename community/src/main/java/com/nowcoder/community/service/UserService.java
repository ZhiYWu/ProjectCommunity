package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 针对每一个查出来的userId，我们不能直接把这个ID显示到用户的页面上
     * 通过该方法将用户查出来进行返回。
     * 利用这样的方法后期可以和Redis进行结合，效率更高
     * @param id
     * @return
     */
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

}
