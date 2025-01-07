package org.zkw.rpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhoukewei
 * @CreateTime: 2025-01-07
 *
 * 协议消息结构
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolMessage<T> {

    /**
     * 消息头
     */
    private Header header;

    /**
     * 消息体
     */
    private T body;

    @Data
    public static class Header {

        /**
         * 魔数
         * 8bit
         */
        private byte magic;

        /**
         * 版本
         * 8bit
         */
        private byte version;

        /**
         * 序列化方式
         * 8bit
         */
        private byte serializer;

        /**
         * 消息类型（请求/响应）
         * 8bit
         */
        private byte type;

        /**
         * 状态
         * 8bit
         */
        private byte status;

        /**
         * 请求ID
         * 64bit
         */
        private long requestId;

        /**
         * 请求体数据长度
         * 32bit
         */
        private int bodyLength;
    }

}
