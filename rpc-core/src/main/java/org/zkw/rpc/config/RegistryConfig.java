package org.zkw.rpc.config;

import lombok.Data;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-06
 *
 * RPC 框架注册中心配置
 */
@Data
public class RegistryConfig {

    private String registry = "etcd";
    private String address = "http://localhost:2380";
    private String username;
    private String password;
    private Long timeout = 10000L;
}
