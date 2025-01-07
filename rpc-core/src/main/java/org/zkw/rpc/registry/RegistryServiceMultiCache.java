package org.zkw.rpc.registry;

import org.zkw.rpc.model.ServiceMetaInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-07
 */
public class RegistryServiceMultiCache {

    Map<String, Map<String, ServiceMetaInfo>> serviceCache = new ConcurrentHashMap<>();

    void writeCache(String serviceKey, String serviceNodeKey, ServiceMetaInfo serviceMetaInfo) {
        if (!serviceCache.containsKey(serviceKey)) {
            Map<String, ServiceMetaInfo> metaInfoMap = new HashMap<>();
            metaInfoMap.put(serviceNodeKey, serviceMetaInfo);
            serviceCache.put(serviceKey, metaInfoMap);
        } else {
            Map<String, ServiceMetaInfo> metaInfoMap = serviceCache.get(serviceKey);
            metaInfoMap.put(serviceNodeKey, serviceMetaInfo);
        }
    }

    List<ServiceMetaInfo> readCache(String serviceKey) {
        Map<String, ServiceMetaInfo> metaInfoMap = serviceCache.get(serviceKey);
        return metaInfoMap == null ? null : new ArrayList<>(metaInfoMap.values());
    }

    void clearCache(String serviceKey, String serviceNodeKey) {
        Map<String, ServiceMetaInfo> metaInfoMap = serviceCache.get(serviceKey);
        if (metaInfoMap == null) {
            return;
        }
        metaInfoMap.remove(serviceNodeKey);
    }

    boolean containsKey(String serviceKey) {
        return serviceCache.containsKey(serviceKey);
    }

    boolean containsCache(String serviceKey, String serviceNodeKey) {
        Map<String, ServiceMetaInfo> metaInfoMap = serviceCache.get(serviceKey);
        if (metaInfoMap == null) {
            return false;
        }
        return metaInfoMap.containsKey(serviceNodeKey);
    }
}
