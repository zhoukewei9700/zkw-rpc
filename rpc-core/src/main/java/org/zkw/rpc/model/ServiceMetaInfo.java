package org.zkw.rpc.model;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-06
 * 服务元信息（注册信息）
 */
@Data
public class ServiceMetaInfo {

    private String serviceName;
    private String serviceVersion = "1.0";
    private String serviceHost;
    private Integer servicePort;
    private String serviceGroup = "default";

    /**
     * 获取服务键名
     *
     * @return
     */
    public String getServiceKey() {
        // 后续可扩展服务分组
        // return String.format("%s:%s:%s", serviceName, serviceVersion, serviceGroup);
        return String.format("%s:%s", serviceName, serviceVersion);
    }

    /**
     * 获取服务注册节点键名
     *
     * @return
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePort);
    }

    /**
     * 获取完整服务地址
     *
     * @return
     */
    public String getServiceAddress() {
        if (!StrUtil.contains(serviceHost, "http")) {
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }


}
