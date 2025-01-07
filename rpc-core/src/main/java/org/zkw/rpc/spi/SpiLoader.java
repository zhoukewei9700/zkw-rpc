package org.zkw.rpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.zkw.rpc.serializer.Serializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-06
 */
@Slf4j
public class SpiLoader {

    /**
     * 存储已加载的类：接口名 => (Key => 实现类)
     */
    private static final Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();


    /**
     * 对象实例缓存（避免重复new），类路径 => 对象实例，单例模式
     */
    private static final Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * 系统 SPI 目录
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     * 用户自定义 SPI 目录
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_CUSTOM_SPI_DIR, RPC_SYSTEM_SPI_DIR};


    /**
     * 获取某个接口的实例
     * @param tClass
     * @param key
     * @return
     * @param <T>
     */
    public static <T> T getInstance(Class<?> tClass, String key) {
        String tClassName = tClass.getName();

        // 优化：改成 ConcurrentHashMap 的 computeIfAbsent 方法，减小锁的粒度，提升性能
        loaderMap.computeIfAbsent(tClassName, k -> load(tClass));

        // 双检索懒加载
        // 检查是否已经加载对应类型
//        if (!loaderMap.containsKey(tClassName)) {
//            synchronized (loaderMap) {
//                // double check, 确保其他线程没有加载
//                if (!loaderMap.containsKey(tClassName)) {
////                    log.info("首次加载 {} 类型的 SPI 配置", tClassName);
//                    load(tClass);
//                }
//            }
//        }

        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);
        if (!keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoader 的 %s 不存在 key=%s 的类型", tClassName, key));
        }
        // 获取到要加载的实现类型
        Class<?> implClass = keyClassMap.get(key);
        // 从实例缓存中加载指定类型的实例
        String implClassName = implClass.getName();
        // 优化：改成 ConcurrentHashMap 的 computeIfAbsent 方法，减小锁的粒度，提升性能
        instanceCache.computeIfAbsent(implClassName, k -> {
            try {
                return implClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                String errMsg = String.format("%s 类实例化失败", implClassName);
                throw new RuntimeException(errMsg, e);
            }
        });
        // 双检索懒加载
//        if (!instanceCache.containsKey(implClassName)) {
//            synchronized (instanceCache) {
//                if (!instanceCache.containsKey(implClassName)) {
//                    try {
//                        instanceCache.put(implClassName, implClass.getDeclaredConstructor().newInstance());
//                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
//                             InvocationTargetException e) {
//                        String errMsg = String.format("%s 类实例化失败", implClassName);
//                        throw new RuntimeException(errMsg, e);
//                    }
//                }
//            }
//        }
        return (T) instanceCache.get(implClassName);
    }

    /**
     * 加载某个类型
     *
     * @param loadClass
     * @return
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("加载类型为 {} 的 SPI", loadClass.getName());
        // 扫描路径， 用户自定义的SPI优先级高于系统SPI
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        for (String scanDir : SCAN_DIRS) {
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            // 读取每个资源文件
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] strArray = line.split("=");
                        if (strArray.length > 1) {
                            String key = strArray[0].trim();
                            String className = strArray[1].trim();
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    log.error("spi resource load error", e);
                }
            }
        }
//        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }

}
