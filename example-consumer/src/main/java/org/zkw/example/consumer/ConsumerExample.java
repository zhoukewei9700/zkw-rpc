package org.zkw.example.consumer;

import org.zkw.rpc.config.RpcConfig;
import org.zkw.rpc.utils.ConfigUtils;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-05
 */
public class ConsumerExample {

    public static void main(String[] args) {
        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
//        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc", "", ".yaml");
        System.out.println(rpc);
    }
}
