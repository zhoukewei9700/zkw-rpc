package org.zkw.rpc.protocol;

import io.vertx.core.buffer.Buffer;
import org.zkw.rpc.model.RpcRequest;
import org.zkw.rpc.model.RpcResponse;
import org.zkw.rpc.serializer.Serializer;
import org.zkw.rpc.serializer.SerializerFactory;

import java.io.IOException;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-07
 */
public class ProtocolMessageDecoder {

    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {
        // 分别从指定位置读取Buffer
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        byte magic = buffer.getByte(0);
        // 校验魔数
        if (ProtocolConstant.PROTOCOL_MAGIC != magic) {
            throw new RuntimeException("消息magic非法");
        }
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));
        // 解决粘包问题，只读取指定长度的数据
        int bodyLength = buffer.getInt(13);
        header.setBodyLength(bodyLength);
        byte[] bodyBytes = buffer.getBytes(17, 17 + bodyLength);
        // 解析消息体
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化消息的协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        ProtocolMessageTypeEnum typeEnum = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
        if (typeEnum == null) {
            throw new RuntimeException("序列化消息的类型不存在");
        }
        switch (typeEnum) {
            case REQUEST: {
                RpcRequest request = serializer.deserialize(bodyBytes, RpcRequest.class);
                return new ProtocolMessage<>(header, request);
            }
            case RESPONSE: {
                RpcResponse response = serializer.deserialize(bodyBytes, RpcResponse.class);
                return new ProtocolMessage<>(header, response);
            }
            case HEART_BEAT:
            case OTHERS:
            default:
                throw new RuntimeException("暂不支持该消息类型");

        }
    }
}
