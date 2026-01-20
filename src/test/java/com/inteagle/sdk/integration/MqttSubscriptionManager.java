/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.integration;

import com.inteagle.sdk.entity.Entity;
import com.inteagle.sdk.model.VdmTelemetry;
import com.inteagle.sdk.mqtt.BridgeMessage;
import com.inteagle.sdk.mqtt.MessageParser;
import com.inteagle.sdk.mqtt.MqttSubscriber;
import com.inteagle.sdk.parser.ParserRegistry;
import com.inteagle.sdk.parser.VdmParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MQTT 订阅管理器 - 用于交互式模式
 * <p>
 * 在后台线程中管理 MQTT 订阅，不阻塞交互式 shell 输入。
 */
public class MqttSubscriptionManager {

    private static final Logger log = LoggerFactory.getLogger(MqttSubscriptionManager.class);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private MqttSubscriber subscriber;
    private volatile boolean running = false;
    private String currentTopic;
    private final AtomicInteger messageCount = new AtomicInteger(0);
    private final AtomicLong startTime = new AtomicLong(0);

    private final OutputFormatter formatter;

    /**
     * 构造函数
     *
     * @param formatter 输出格式化器
     */
    public MqttSubscriptionManager(OutputFormatter formatter) {
        this.formatter = formatter;

        // 注册 VDM 协议解析器
        if (ParserRegistry.getInstance().getRegisteredProfiles().isEmpty()) {
            ParserRegistry.getInstance().register(new VdmParser());
            log.debug("Registered VDM parser");
        }
    }

    /**
     * 启动 MQTT 订阅（后台线程）
     *
     * @param brokerHost MQTT broker 地址
     * @param brokerPort MQTT broker 端口
     * @param accessKey  访问密钥
     * @param secretKey  密钥
     * @param customerId 客户ID
     * @param useTls     是否使用 TLS
     * @param topic      订阅主题
     * @throws Exception 连接异常
     */
    public void start(String brokerHost, int brokerPort, String accessKey, String secretKey,
                      String customerId, boolean useTls, String topic) throws Exception {
        if (running) {
            throw new IllegalStateException("订阅已在运行中");
        }

        // 创建 MQTT Subscriber
        subscriber = new MqttSubscriber(brokerHost, brokerPort, accessKey, secretKey, customerId, useTls);

        // 设置连接事件监听
        subscriber.setConnectionEventListener(event -> {
            switch (event.getType()) {
                case CONNECTED -> formatter.printSuccess("✓ MQTT 已连接");
                case DISCONNECTED -> formatter.printWarning("⚠ MQTT 连接断开: " + event.getMessage());
                case RECONNECTED -> formatter.printInfo("↻ MQTT 重连成功 (第" + event.getReconnectCount() + "次)");
            }
        });

        // 连接
        formatter.printInfo("正在连接 MQTT Broker " + brokerHost + ":" + brokerPort + "...");
        subscriber.connect();

        // 订阅
        currentTopic = topic;
        subscriber.subscribe(topic, this::handleMessage);

        running = true;
        startTime.set(System.currentTimeMillis());
        messageCount.set(0);

        formatter.printSuccess("✓ 已订阅: " + topic);
        formatter.printInfo("实时消息将显示在下方 (输入命令不受影响)");
        formatter.printSeparator();
    }

    /**
     * 停止 MQTT 订阅
     */
    public void stop() {
        if (!running) {
            formatter.printWarning("订阅未运行");
            return;
        }

        try {
            if (subscriber != null) {
                subscriber.disconnect();
                subscriber.close();
            }
            running = false;

            long duration = (System.currentTimeMillis() - startTime.get()) / 1000;
            formatter.printSeparator();
            formatter.printSuccess("✓ 已停止订阅");
            formatter.printInfo("共收到 " + messageCount.get() + " 条消息，运行时长 " + duration + " 秒");
        } catch (Exception e) {
            formatter.printError("停止订阅失败: " + e.getMessage());
        }
    }

    /**
     * 获取订阅状态
     */
    public SubscriptionStatus getStatus() {
        if (!running) {
            return new SubscriptionStatus(false, null, 0, 0);
        }

        long duration = (System.currentTimeMillis() - startTime.get()) / 1000;
        return new SubscriptionStatus(
                true,
                currentTopic,
                messageCount.get(),
                duration
        );
    }

    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * 处理接收到的消息
     */
    private void handleMessage(BridgeMessage message) {
        int count = messageCount.incrementAndGet();

        try {
            String time = TIME_FMT.format(Instant.ofEpochMilli(message.getTs()));
            Entity entity = message.getEntity();

            // 消息头
            StringBuilder sb = new StringBuilder();
            sb.append(formatter.dim("[" + time + "]"));
            sb.append(" ");
            sb.append(formatter.cyan("#" + count));
            sb.append(" ");
            sb.append(formatter.bold(message.getType().toString()));

            if (entity != null) {
                sb.append(" ");
                sb.append(formatter.dim(entity.getType() + "/" + entity.getId()));
            }

            System.out.println(sb);

            // 如果有 entity 且支持协议解析器，则解析数据
            if (entity != null && MessageParser.hasProtocolParser(message)) {
                String profile = entity.getProfile();
                System.out.println("  " + formatter.dim("Profile: " + profile));

                try {
                    VdmTelemetry vdm = MessageParser.parseVdm(message);
                    if (vdm != null) {
                        printVdmTelemetry(vdm);
                    }
                } catch (Exception e) {
                    System.out.println("  " + formatter.yellow("⚠ 解析失败: " + e.getMessage()));
                }
            } else {
                // 显示原始 payload
                String payload = message.getPayload().toString();
                if (payload.length() > 100) {
                    payload = payload.substring(0, 97) + "...";
                }
                System.out.println("  " + formatter.dim(payload));
            }

            System.out.println(); // 空行分隔

        } catch (Exception e) {
            log.error("Error handling message", e);
        }
    }

    /**
     * 格式化显示 VDM 遥测数据
     */
    private void printVdmTelemetry(VdmTelemetry vdm) {
        System.out.println("  " + formatter.green("✓ VDM 数据:"));

        // 位移
        if (vdm.hasDisplacement()) {
            if (vdm.getDx() != null) {
                System.out.println("    • dx = " + formatter.cyan(String.format("%.2f", vdm.getDx())) + " mm");
            }
            if (vdm.getDy() != null) {
                System.out.println("    • dy = " + formatter.cyan(String.format("%.2f", vdm.getDy())) + " mm");
            }
            if (vdm.getDz() != null) {
                System.out.println("    • dz = " + formatter.cyan(String.format("%.2f", vdm.getDz())) + " mm");
            }
        }

        // 角度
        if (vdm.getAngle() != null) {
            System.out.println("    • angle = " + formatter.cyan(String.format("%.1f", vdm.getAngle())) + " °");
        }

        // 温度
        if (vdm.hasTemperature()) {
            System.out.println("    • temp = " + formatter.cyan(String.format("%.1f", vdm.getTemperature())) + " °C");
        }

        // 电池
        if (vdm.hasBattery()) {
            String batteryStr = vdm.getBattery() + " %";
            if (vdm.isBatteryLow()) {
                batteryStr = formatter.yellow(batteryStr + " (低电量)");
            } else {
                batteryStr = formatter.cyan(batteryStr);
            }
            System.out.println("    • battery = " + batteryStr);
        }

        // 信号强度
        if (vdm.getSignal() != null) {
            System.out.println("    • signal = " + formatter.cyan(vdm.getSignal().toString()) + " dBm");
        }

        // 状态
        if (vdm.getStatus() != null) {
            System.out.println("    • status = " + formatter.cyan(vdm.getStatus().toString()));
        }
    }

    /**
     * 订阅状态
     */
    public static class SubscriptionStatus {
        private final boolean running;
        private final String topic;
        private final int messageCount;
        private final long durationSeconds;

        public SubscriptionStatus(boolean running, String topic, int messageCount, long durationSeconds) {
            this.running = running;
            this.topic = topic;
            this.messageCount = messageCount;
            this.durationSeconds = durationSeconds;
        }

        public boolean isRunning() {
            return running;
        }

        public String getTopic() {
            return topic;
        }

        public int getMessageCount() {
            return messageCount;
        }

        public long getDurationSeconds() {
            return durationSeconds;
        }
    }

    /**
     * 输出格式化器接口
     */
    public interface OutputFormatter {
        void printSuccess(String msg);
        void printInfo(String msg);
        void printWarning(String msg);
        void printError(String msg);
        void printSeparator();
        String bold(String text);
        String cyan(String text);
        String green(String text);
        String yellow(String text);
        String dim(String text);
    }
}
