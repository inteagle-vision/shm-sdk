/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.mqtt;

import com.inteagle.sdk.mqtt.data.AlarmData;
import com.inteagle.sdk.mqtt.data.EventData;
import com.inteagle.sdk.mqtt.data.ImageData;
import com.inteagle.sdk.mqtt.data.TelemetryData;

/**
 * 消息解析器 - 将 BridgeMessage 转换为强类型数据对象
 *
 * <p>使用示例:
 * <pre>{@code
 * subscriber.subscribe("inteagle/customer1/#", msg -> {
 *     switch (msg.getType()) {
 *         case TELEMETRY:
 *             TelemetryData telemetry = MessageParser.parseTelemetry(msg);
 *             Double temp = telemetry.getDouble("temperature");
 *             break;
 *         case ALARM:
 *             AlarmData alarm = MessageParser.parseAlarm(msg);
 *             if (alarm.isCritical()) {
 *                 // 处理严重告警
 *             }
 *             break;
 *         case EVENT:
 *             EventData event = MessageParser.parseEvent(msg);
 *             String eventType = event.getEventType();
 *             break;
 *     }
 * });
 * }</pre>
 *
 * <p>或使用统一的 parse 方法:
 * <pre>{@code
 * Object data = MessageParser.parse(msg);
 * if (data instanceof TelemetryData) {
 *     TelemetryData telemetry = (TelemetryData) data;
 * }
 * }</pre>
 */
public class MessageParser {

    private MessageParser() {
        // 工具类，禁止实例化
    }

    /**
     * 根据消息类型自动解析为对应的数据对象
     *
     * @param message BridgeMessage
     * @return TelemetryData, AlarmData, EventData, ImageData 或原始 BridgeMessage
     */
    public static Object parse(BridgeMessage message) {
        if (message == null) return null;

        switch (message.getType()) {
            case TELEMETRY:
                return parseTelemetry(message);
            case ALARM:
                return parseAlarm(message);
            case EVENT:
                return parseEvent(message);
            case IMAGE:
                return parseImage(message);
            default:
                return message;
        }
    }

    /**
     * 解析为遥测数据
     */
    public static TelemetryData parseTelemetry(BridgeMessage message) {
        return TelemetryData.from(
                message.getTopic(),
                message.getTs(),
                message.getPayload()
        );
    }

    /**
     * 解析为告警数据
     */
    public static AlarmData parseAlarm(BridgeMessage message) {
        return AlarmData.from(
                message.getTopic(),
                message.getTs(),
                message.getPayload()
        );
    }

    /**
     * 解析为事件数据
     */
    public static EventData parseEvent(BridgeMessage message) {
        return EventData.from(
                message.getTopic(),
                message.getTs(),
                message.getPayload()
        );
    }

    /**
     * 解析为图像数据
     */
    public static ImageData parseImage(BridgeMessage message) {
        return ImageData.from(
                message.getTopic(),
                message.getTs(),
                message.getPayload()
        );
    }

    /**
     * 检查消息是否为遥测数据
     */
    public static boolean isTelemetry(BridgeMessage message) {
        return message != null && message.getType() == MessageType.TELEMETRY;
    }

    /**
     * 检查消息是否为告警数据
     */
    public static boolean isAlarm(BridgeMessage message) {
        return message != null && message.getType() == MessageType.ALARM;
    }

    /**
     * 检查消息是否为事件数据
     */
    public static boolean isEvent(BridgeMessage message) {
        return message != null && message.getType() == MessageType.EVENT;
    }

    /**
     * 检查消息是否为图像数据
     */
    public static boolean isImage(BridgeMessage message) {
        return message != null && message.getType() == MessageType.IMAGE;
    }
}
