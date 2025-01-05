package org.zkw.example.common.service;

import org.zkw.example.common.model.User;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-05
 */
public interface UserService {

    /**
     * 获取用户
     *
     * @param user
     * @return
     */
    User getUser(User user);

    /**
     * 用于测试 mock 接口返回值
     *
     * @return
     */
    default short getNumber() {
        return 1;
    }
}
