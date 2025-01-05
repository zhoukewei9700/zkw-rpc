package org.zkw.example.provider;

import org.zkw.example.common.model.User;
import org.zkw.example.common.service.UserService;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-05
 */
public class UserServiceImpl implements UserService {

    @Override
    public User getUser(User user) {
        System.out.println("用户名：" + user.getName());
        return user;
    }
}
