package org.zkw.rpc.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.setting.yaml.YamlUtil;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-05
 * 配置工具类
 */
public class ConfigUtils {

    /**
     * 加载配置对象
     * @param tClass
     * @param prefix
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
    }

    /**
     * 加载配置对象，支持区分环境
     * @param tClass
     * @param prefix
     * @param environment
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".properties");
        Props props = new Props(configFileBuilder.toString());
        // 监听配置文件变更
        props.autoLoad(true);
        return props.toBean(tClass, prefix);
    }

    private static final String EXTENSION_PROPERTIES = ".properties";
    private static final String EXTENSION_YAML = ".yaml";
    private static final String EXTENSION_YML = ".yml";

    /**
     * 加载配置对象， 支持不同文件扩展名
     * @param tClass
     * @param prefix
     * @param environment
     * @param extension
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment, String extension) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(extension);
        switch (extension) {
            case EXTENSION_PROPERTIES: {
                Props props = new Props(configFileBuilder.toString());
                return props.toBean(tClass, prefix);
            }
            case EXTENSION_YAML:
            case EXTENSION_YML: {
                return YamlUtil.loadByPath(configFileBuilder.toString(), tClass);
            }
            default: {
                throw new IllegalArgumentException("配置文件类型错误，只能为.properties, .yaml或.yml");
            }
        }
    }
}
