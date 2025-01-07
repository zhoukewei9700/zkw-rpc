package org.zkw.rpc.registry;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;
import org.zkw.rpc.config.RegistryConfig;
import org.zkw.rpc.model.ServiceMetaInfo;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-06
 */
@Slf4j
public class EtcdRegistry implements Registry{

    private Client client;
    private KV kvClient;

    /**
     * 本机注册的节点key集合（用于维护续期）
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    @Deprecated
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    private final RegistryServiceMultiCache registryServiceMultiCache = new RegistryServiceMultiCache();

    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        heartBeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建 Lease 和 KV 客户端
        Lease leaseClient = client.getLeaseClient();

        // 创建一个 30 秒的租约
        long leaseId = leaseClient.grant(30).get().getID();

        // 设置要存储的键值对
        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registryKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        // 存储，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        // 添加节点信息到本地缓存
        localRegisterNodeKeySet.add(registryKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        try {
            kvClient.delete(ByteSequence.from(registryKey, StandardCharsets.UTF_8)).get();
        } catch (Exception e) {
            throw new RuntimeException("删除服务失败");
        }
        localRegisterNodeKeySet.remove(registryKey);

    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 从缓存中获取服务
//        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceMultiCache.readCache(serviceKey);
        if (cachedServiceMetaInfoList != null) {
            return cachedServiceMetaInfoList;
        }

        // prefix search
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

        try {
            // 第一次访问该服务才监听
            if (!registryServiceMultiCache.containsKey(serviceKey)) {
                watch(serviceKey);
            }
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption)
                    .get()
                    .getKvs();
            // 解析服务信息
            return keyValues.stream()
                    .map(keyValue -> {
                        String serviceNodeKey = keyValue.getKey().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(keyValue.getValue().toString(StandardCharsets.UTF_8), ServiceMetaInfo.class);
                        registryServiceMultiCache.writeCache(serviceKey, serviceNodeKey, serviceMetaInfo);
                        return serviceMetaInfo;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    @Override
    public void destroy() {
        System.out.println("当前节点下线");

        for (String key: localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }
        // release resources
        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }

    @Override
    public void heartBeat() {
        // 10s 续签一次
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历本节点所有Key
                for (String key : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();
                        if (keyValues == null || keyValues.isEmpty()) {
                            continue;
                        }
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                        throw new RuntimeException(key + " 续签失败", e);
                    }
                }
            }
        });

        // 支持秒级定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    /**
     * 监听（消费端）
     *
     * @param serviceKey
     */
    @Override
    public void watch(String serviceKey) {
        Watch watchClient = client.getWatchClient();
        // 之前未被监听，开启监听
        boolean newWatch = watchingKeySet.add(serviceKey);
        if (newWatch) {
            WatchOption watchOption = WatchOption.builder().isPrefix(true).build();
            watchClient.watch(ByteSequence.from(ETCD_ROOT_PATH + serviceKey, StandardCharsets.UTF_8), watchOption, response -> {
                for (WatchEvent event : response.getEvents()) {
                    KeyValue keyValue = event.getKeyValue();
                    String watchServiceNodeKey = keyValue.getKey().toString(StandardCharsets.UTF_8);
                    switch (event.getEventType()) {
                        case DELETE: {
                            registryServiceMultiCache.clearCache(serviceKey, watchServiceNodeKey);
                            break;
                        }
                        case PUT: {
                            if (registryServiceMultiCache.containsCache(serviceKey, watchServiceNodeKey)) {
                                break;
                            }
                            ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(keyValue.getValue().toString(StandardCharsets.UTF_8), ServiceMetaInfo.class);
                            registryServiceMultiCache.writeCache(serviceKey, watchServiceNodeKey, serviceMetaInfo);
                        }

                    }
                }
            });
        }
    }

}
