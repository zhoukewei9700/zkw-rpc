package org.zkw.example.consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.zkw.example.common.model.User;
import org.zkw.example.common.service.UserService;
import org.zkw.rpc.model.RpcRequest;
import org.zkw.rpc.model.RpcResponse;
import org.zkw.rpc.serializer.JdkSerializer;
import org.zkw.rpc.serializer.Serializer;

import java.io.IOException;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-05
 *
 * 静态代理
 */
public class UserServiceProxy implements UserService {
    // 指定序列化器
    private final Serializer serializer = new JdkSerializer();

    // url
    private static final String URL = "http://localhost:8080";

    @Override
    public User getUser(User user) {
        // 发请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();
        return (User) getRpcResponseData(rpcRequest);
    }

    @Override
    public short getNumber() {
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getNumber")
                .parameterTypes(null)
                .args(null)
                .build();
        Object result = getRpcResponseData(rpcRequest);
        if (result == null) {
            return 0;
        } else {
            return (short) result;
        }
    }

    private Object getRpcResponseData(RpcRequest rpcRequest) {
        try {
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            byte[] result;
            try (HttpResponse httpResponse = HttpRequest.post(URL)
                    .body(bodyBytes)
                    .execute()) {
                result = httpResponse.bodyBytes();
            }
            RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
            return rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
