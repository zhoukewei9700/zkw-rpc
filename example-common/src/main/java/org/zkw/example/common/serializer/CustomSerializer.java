package org.zkw.example.common.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zkw.rpc.model.RpcRequest;
import org.zkw.rpc.model.RpcResponse;
import org.zkw.rpc.serializer.Serializer;

import java.io.IOException;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-06
 */
public class CustomSerializer implements Serializer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        T object = OBJECT_MAPPER.readValue(bytes, type);
        if (object instanceof RpcRequest) {
            return handleRequest((RpcRequest) object, type);
        }
        if (object instanceof RpcResponse) {
            return handleResponse((RpcResponse) object, type);
        }
        return object;
    }

    /**
     * 由于 Object 的原始对象会被擦除，导致反序列化时会被作为 LinkedHashMap 无法转换为原始对象，因此这里做特殊处理
     * @param rpcRequest
     * @param type
     * @return
     * @param <T>
     * @throws IOException
     */
    private <T> T handleRequest(RpcRequest rpcRequest, Class<T> type) throws IOException{
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] args = rpcRequest.getArgs();

        // 循环处理每个参数类型
        for (int i = 0; i < parameterTypes.length; ++i) {
            Class<?> clazz = parameterTypes[i];
            // 如果类型不同，重新处理类型
            if (!clazz.isAssignableFrom(args[i].getClass())) {
                byte[] argBytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i] = OBJECT_MAPPER.readValue(argBytes, clazz);
            }
        }
        return type.cast(rpcRequest);
    }

    /**
     * 由于 Object 的原始对象会被擦除，导致反序列化时会被作为 LinkedHashMap 无法转换为原始对象，因此这里做特殊处理
     * @param rpcResponse
     * @param type
     * @return
     * @param <T>
     * @throws IOException
     */
    private <T> T handleResponse(RpcResponse rpcResponse, Class<T> type) throws IOException {
        byte[] dataBytes = OBJECT_MAPPER.writeValueAsBytes(rpcResponse.getData());
        rpcResponse.setData(OBJECT_MAPPER.readValue(dataBytes, rpcResponse.getDataType()));
        return type.cast(rpcResponse);
    }
}
