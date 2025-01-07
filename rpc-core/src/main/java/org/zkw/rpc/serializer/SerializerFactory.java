package org.zkw.rpc.serializer;

import org.zkw.rpc.spi.SpiLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-06
 */
public class SerializerFactory {

    private static volatile Serializer defaultSerializer;

    private static final ConcurrentMap<String, Serializer> serializerCache = new ConcurrentHashMap<>();

    private static final ReentrantLock lock = new ReentrantLock();

    /**
     * 获取默认序列化器
     *
     * @return 默认序列化器
     */
    public static Serializer getDefaultSerializer() {
        // 双重检查锁实现懒加载
        if (defaultSerializer == null) {
            lock.lock();
            try {
                if (defaultSerializer == null) {
                    defaultSerializer = new JdkSerializer();
                }
            } finally {
                lock.unlock();
            }
        }
        return defaultSerializer;
    }

    /**
     * 获取实例
     *
     * @param key 序列化器的 key
     * @return 序列化器实例
     */
    public static Serializer getInstance(String key) {
        return serializerCache.computeIfAbsent(key, k -> SpiLoader.getInstance(Serializer.class, k));
    }

//    static {
//        SpiLoader.load(Serializer.class);
//    }

//    private static final Map<String, Serializer> KEY_SERIALIZER_MAP = new HashMap<String, Serializer>() {{
//        put(SerializerKeys.JDK, new JdkSerializer());
//        put(SerializerKeys.JSON, new JsonSerializer());
//        put(SerializerKeys.KRYO, new KryoSerializer());
//        put(SerializerKeys.HESSIAN, new HessianSerializer());
//    }};
//
//    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();
//
//    public static Serializer getInstance(String key) {
//        return SpiLoader.getInstance(Serializer.class, key);
//    }
}
