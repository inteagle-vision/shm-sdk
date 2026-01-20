/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.mqtt;

import com.inteagle.sdk.model.VdmTelemetry;
import com.inteagle.sdk.mqtt.data.AlarmData;
import com.inteagle.sdk.mqtt.data.EventData;
import com.inteagle.sdk.mqtt.data.ImageData;
import com.inteagle.sdk.mqtt.data.TelemetryData;
import com.inteagle.sdk.parser.ParserException;
import com.inteagle.sdk.parser.ParserRegistry;
import com.inteagle.sdk.parser.ProtocolParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(MessageParser.class);

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

    // ==================== Protocol Parser Support ====================

    /**
     * Parse telemetry using protocol parser based on entity profile
     * <p>
     * This method uses the ParserRegistry to find a suitable parser for the
     * device profile specified in the message's entity field.
     * <p>
     * If no profile is specified or no parser is registered, returns null.
     *
     * @param message BridgeMessage with entity information
     * @return Parsed telemetry data (e.g., VdmTelemetry), or null if parsing fails
     */
    public static Object parseWithProtocol(BridgeMessage message) {
        if (message == null || message.getEntity() == null) {
            log.debug("Message or entity is null, cannot use protocol parser");
            return null;
        }

        String profile = message.getEntity().getProfile();
        if (profile == null || profile.isEmpty()) {
            log.debug("Entity profile is null or empty, cannot use protocol parser");
            return null;
        }

        ParserRegistry registry = ParserRegistry.getInstance();
        ProtocolParser<?> parser = registry.getParser(profile);

        if (parser == null) {
            log.debug("No parser registered for profile: {}", profile);
            return null;
        }

        try {
            return parser.parse(profile, message.getPayload());
        } catch (ParserException e) {
            log.error("Failed to parse message with protocol parser: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parse telemetry as VdmTelemetry
     * <p>
     * Convenience method for parsing VDM device telemetry.
     * Uses the VdmParser if the profile is supported.
     *
     * @param message BridgeMessage with VDM entity
     * @return VdmTelemetry, or null if parsing fails or profile is not VDM
     */
    public static VdmTelemetry parseVdm(BridgeMessage message) {
        Object result = parseWithProtocol(message);
        if (result instanceof VdmTelemetry) {
            return (VdmTelemetry) result;
        }
        return null;
    }

    /**
     * Check if message can be parsed with a protocol parser
     *
     * @param message BridgeMessage
     * @return true if a parser is available for the message's profile
     */
    public static boolean hasProtocolParser(BridgeMessage message) {
        if (message == null || message.getEntity() == null) {
            return false;
        }

        String profile = message.getEntity().getProfile();
        if (profile == null || profile.isEmpty()) {
            return false;
        }

        return ParserRegistry.getInstance().hasParser(profile);
    }
}
