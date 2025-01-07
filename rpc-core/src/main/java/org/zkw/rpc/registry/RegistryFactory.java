package org.zkw.rpc.registry;

import org.zkw.rpc.serializer.JdkSerializer;
import org.zkw.rpc.serializer.Serializer;
import org.zkw.rpc.spi.SpiLoader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-06
 */
public class RegistryFactory {
    private static volatile Registry defaultRegistry;

    private static final ConcurrentMap<String, Registry> registryCache = new ConcurrentHashMap<>();

    private static final ReentrantLock lock = new ReentrantLock();

    /**
     * 获取默认注册中心
     *
     * @return 默认注册中心
     */
    public static Registry getDefaultRegistry() {
        // 双重检查锁实现懒加载
        if (defaultRegistry == null) {
            lock.lock();
            try {
                if (defaultRegistry == null) {
                    defaultRegistry = new EtcdRegistry();
                }
            } finally {
                lock.unlock();
            }
        }
        return defaultRegistry;
    }

    /**
     * 获取实例
     *
     * @param key 序列化器的 key
     * @return 序列化器实例
     */
    public static Registry getInstance(String key) {
        return registryCache.computeIfAbsent(key, k -> SpiLoader.getInstance(Registry.class, k));
    }
}
