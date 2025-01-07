package org.zkw.example.provider;

import org.zkw.example.common.service.UserService;
import org.zkw.rpc.RpcApplication;
import org.zkw.rpc.config.RegistryConfig;
import org.zkw.rpc.config.RpcConfig;
import org.zkw.rpc.model.ServiceMetaInfo;
import org.zkw.rpc.registry.LocalRegistry;
import org.zkw.rpc.registry.Registry;
import org.zkw.rpc.registry.RegistryFactory;
import org.zkw.rpc.server.Server;
import org.zkw.rpc.server.http.VertxHttpServer;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-05
 */
public class ProviderExample {

    public static void main(String[] args) {
        // RPC 框架初始化
        RpcApplication.init();

        // 注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 启动 web 服务
        Server httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
