package org.zkw.example.provider;

import org.zkw.example.common.service.UserService;
import org.zkw.rpc.registry.LocalRegistry;
import org.zkw.rpc.server.HttpServer;
import org.zkw.rpc.server.VertxHttpServer;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-05
 */
public class EasyProviderExample {
    public static void main(String[] args) {
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 启动web服务
        HttpServer server = new VertxHttpServer();
        server.doStart(8080);
    }
}
