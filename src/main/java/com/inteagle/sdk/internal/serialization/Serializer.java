/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.serialization;

import java.util.List;
import java.util.Map;

/**
 * 序列化器接口
 * <p>
 * 提供序列化/反序列化抽象，支持 JSON 和未来的 Protobuf。
 * <p>
 * 设计要点:
 * <ul>
 *   <li>协议无关: 传输层只依赖此接口</li>
 *   <li>类型安全: 支持泛型反序列化</li>
 *   <li>可扩展: 通过实现此接口支持新的序列化格式</li>
 * </ul>
 */
public interface Serializer {

    /**
     * 序列化对象为字符串
     *
     * @param object 要序列化的对象
     * @return 序列化后的字符串
     */
    String serialize(Object object);

    /**
     * 序列化对象为字节数组
     *
     * @param object 要序列化的对象
     * @return 序列化后的字节数组
     */
    byte[] serializeToBytes(Object object);

    /**
     * 反序列化为对象
     *
     * @param data 序列化数据
     * @param type 目标类型
     * @param <T>  类型参数
     * @return 反序列化后的对象
     */
    <T> T deserialize(String data, Class<T> type);

    /**
     * 反序列化为对象
     *
     * @param data 序列化数据
     * @param type 目标类型
     * @param <T>  类型参数
     * @return 反序列化后的对象
     */
    <T> T deserialize(byte[] data, Class<T> type);

    /**
     * 反序列化为列表
     *
     * @param data        序列化数据
     * @param elementType 元素类型
     * @param <T>         元素类型参数
     * @return 反序列化后的列表
     */
    <T> List<T> deserializeList(String data, Class<T> elementType);

    /**
     * 反序列化为 Map
     *
     * @param data      序列化数据
     * @param keyType   键类型
     * @param valueType 值类型
     * @param <K>       键类型参数
     * @param <V>       值类型参数
     * @return 反序列化后的 Map
     */
    <K, V> Map<K, V> deserializeMap(String data, Class<K> keyType, Class<V> valueType);

    /**
     * 获取序列化格式名称
     *
     * @return 格式名称 (如 "JSON", "PROTOBUF")
     */
    String getFormat();
}
