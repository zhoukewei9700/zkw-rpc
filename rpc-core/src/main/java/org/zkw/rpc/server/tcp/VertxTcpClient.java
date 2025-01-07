package org.zkw.rpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-07
 */
public class VertxTcpClient {

    public void start() {
        // 创建实例
        Vertx vertx = Vertx.vertx();

        vertx.createNetClient().connect(8888, "localhost", result -> {
                    if (result.succeeded()) {
                        System.out.println("connected to TCP server");
                        NetSocket socket = result.result();
                        // 发送数据
                        socket.write("Hello Server");
                        // 接收响应
                        socket.handler(buffer -> {
                            System.out.println("Received response from server: " + buffer.toString());
                        });
                    } else {
                        System.err.println("failed to connect to TCP server");
                    }
                });
    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}
