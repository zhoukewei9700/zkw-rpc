package org.zkw.example.consumer;

import org.zkw.example.common.model.User;
import org.zkw.example.common.service.UserService;
import org.zkw.rpc.proxy.ServiceProxyFactory;

import static org.zkw.rpc.proxy.ServiceProxyFactory.getProxy;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-05
 */
public class EasyConsumerExample {
    public static void main(String[] args) {
        // 静态代理
//         UserService userService = new UserServiceProxy();
        // 动态代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
//        UserService userService = null;
        User user = new User();
        user.setName("yupi");
        // 调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}
