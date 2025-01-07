package org.zkw.rpc.server;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-05
 * HTTP 服务器接口
 */

public interface Server {

    /**
     * 启动服务器
     *
     * @param port
     */
    void doStart(int port);
}
