package org.zkw.example.consumer;

import org.zkw.example.common.model.User;
import org.zkw.example.common.service.UserService;
import org.zkw.rpc.config.RpcConfig;
import org.zkw.rpc.proxy.ServiceProxyFactory;
import org.zkw.rpc.utils.ConfigUtils;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-05
 */
public class ConsumerExample {

    public static void main(String[] args) {
        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("yupi");
        // 调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
        long number = userService.getNumber();
        System.out.println(number);
    }
}
