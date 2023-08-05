package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替session对象
 */
@Component
public class HostHolder {

    /**
     * 通过 set方法和 get方法实现线程隔离
     */
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }


    /**
     * 每次用完后将线程中的值清理掉
     */
    public void clear() {
        users.remove();
    }

}
