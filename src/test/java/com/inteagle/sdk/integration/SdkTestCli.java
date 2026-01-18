/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.integration;

import com.inteagle.sdk.InteagleClient;
import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.model.*;
import com.inteagle.sdk.query.*;

import java.io.Console;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * SDK 命令行交互测试工具
 * <p>
 * 运行方式:
 * <pre>
 * # 编译 (包含测试类)
 * mvn test-compile
 *
 * # 运行
 * mvn exec:java -Dexec.mainClass="com.inteagle.sdk.integration.SdkTestCli" \
 *     -Dexec.classpathScope=test
 *
 * # 或者打包后运行
 * java -cp target/test-classes:target/classes:target/dependency/* \
 *     com.inteagle.sdk.integration.SdkTestCli
 * </pre>
 */
public class SdkTestCli {

    private static final String DEFAULT_ENDPOINT = "https://api.shm.inteagle.com";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private InteagleClient client;
    private Scanner scanner;

    public static void main(String[] args) {
        new SdkTestCli().run();
    }

    public void run() {
        scanner = new Scanner(System.in);

        printBanner();

        // 1. 获取连接配置
        String endpoint = prompt("API Endpoint", DEFAULT_ENDPOINT);
        String accessKey = prompt("Access Key (AK)", null);
        String secretKey = promptSecret("Secret Key (SK)");

        if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            println("❌ Access Key 和 Secret Key 不能为空");
            return;
        }

        // 2. 创建客户端
        println("\n🔗 连接到 " + endpoint + " ...");
        try {
            client = InteagleClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .timeout(Duration.ofSeconds(30))
                    .build();
            println("✅ 客户端创建成功 (协议: " + client.getProtocol() + ")");
        } catch (Exception e) {
            println("❌ 创建客户端失败: " + e.getMessage());
            return;
        }

        // 3. 测试连接
        println("\n📡 测试 API 连接...");
        if (!testConnection()) {
            client.close();
            return;
        }

        // 4. 交互式菜单
        runInteractiveMenu();

        // 5. 清理
        client.close();
        println("\n👋 再见!");
    }

    private boolean testConnection() {
        try {
            PageResult<MonitoringProject> projects = client.projects().list(
                    ProjectQuery.builder().pageSize(1).build()
            );
            println("✅ API 连接成功! 找到 " + projects.getTotal() + " 个项目");
            return true;
        } catch (SdkException e) {
            println("❌ API 连接失败: " + e.getErrorCode() + " - " + e.getMessage());
            if (e.isRetryable()) {
                println("   (这是一个可重试的错误)");
            }
            return false;
        }
    }

    private void runInteractiveMenu() {
        while (true) {
            println("\n" + "=".repeat(50));
            println("请选择操作:");
            println("  1. 查询项目列表");
            println("  2. 查询设备列表");
            println("  3. 查询监测点列表");
            println("  4. 查询告警规则");
            println("  5. 查询告警统计");
            println("  6. 查询遥测数据");
            println("  7. 查询活跃告警");
            println("  0. 退出");
            println("=".repeat(50));

            String choice = prompt("请输入选项", "0");

            try {
                switch (choice) {
                    case "1" -> listProjects();
                    case "2" -> listDevices();
                    case "3" -> listPoints();
                    case "4" -> listAlarmRules();
                    case "5" -> showAlarmStats();
                    case "6" -> queryTelemetry();
                    case "7" -> listActiveAlarms();
                    case "0" -> { return; }
                    default -> println("无效选项");
                }
            } catch (SdkException e) {
                println("❌ 操作失败: " + e.getErrorCode() + " - " + e.getMessage());
            }
        }
    }

    private void listProjects() throws SdkException {
        println("\n📋 项目列表:");
        PageResult<MonitoringProject> result = client.projects().list(
                ProjectQuery.builder().pageSize(20).build()
        );
        println("   共 " + result.getTotal() + " 个项目\n");

        if (result.isEmpty()) {
            println("   (无数据)");
            return;
        }

        for (MonitoringProject p : result) {
            println(String.format("   • %s", p.getName()));
            println(String.format("     ID: %s (%s)",
                    p.getId(),
                    Boolean.TRUE.equals(p.getActive()) ? "活跃" : "非活跃"));
        }
    }

    private void listDevices() throws SdkException {
        String projectId = prompt("项目ID (留空查询全部)", "");

        println("\n📱 设备列表:");
        DeviceQuery query = DeviceQuery.builder().pageSize(20);
        if (!projectId.isBlank()) {
            query.projectId(projectId);
        }

        PageResult<Device> result = client.devices().list(query.build());
        println("   共 " + result.getTotal() + " 个设备\n");

        if (result.isEmpty()) {
            println("   (无数据)");
            return;
        }

        for (Device d : result) {
            println(String.format("   • %s", d.getName()));
            println(String.format("     ID: %s",
                    d.getDeviceId() != null ? d.getDeviceId() : "?"));
            println(String.format("     类型: %s | %s",
                    d.getType() != null ? d.getType() : "-",
                    d.isOnline() ? "🟢 在线" : "⚪ 离线"));
        }
    }

    private void listPoints() throws SdkException {
        String projectId = prompt("项目ID (留空查询全部)", "");

        println("\n📍 监测点列表:");
        PointQuery query = PointQuery.builder().pageSize(20);
        if (projectId != null && !projectId.isBlank()) {
            query.projectId(projectId);
        }
        PageResult<MonitoringPoint> result = client.points().list(query.build());
        println("   共 " + result.getTotal() + " 个监测点\n");

        if (result.isEmpty()) {
            println("   (无数据)");
            return;
        }

        for (MonitoringPoint p : result) {
            println(String.format("   • %s", p.getName()));
            println(String.format("     ID: %s | 类型: %s",
                    p.getId(),
                    p.getType() != null ? p.getType() : "-"));
        }
    }

    private void listAlarmRules() throws SdkException {
        String projectId = prompt("项目ID", null);
        if (projectId == null || projectId.isBlank()) {
            println("项目ID 不能为空");
            return;
        }

        println("\n📋 告警规则列表:");
        PageResult<AlarmRule> result = client.alarmRules().list(
                AlarmRuleQuery.ofProject(projectId).pageSize(20).build()
        );
        println("   共 " + result.getTotal() + " 条规则\n");

        if (result.isEmpty()) {
            println("   (无数据)");
            return;
        }

        for (AlarmRule rule : result) {
            String scope = rule.isProjectLevel() ? "项目级" : "实体级";
            println(String.format("   • [%s] %s", rule.getId(), rule.getName()));
            println(String.format("     范围: %s | 类型: %s | %s",
                    scope,
                    rule.getEntityType(),
                    Boolean.TRUE.equals(rule.getEnabled()) ? "已启用" : "已禁用"));

            if (rule.getThresholds() != null && !rule.getThresholds().isEmpty()) {
                println("     阈值 (3A模式):");
                for (AlarmRule.ThresholdRule t : rule.getThresholds()) {
                    println(String.format("       %s: 预警=%s 报警=%s 紧急=%s %s",
                            t.getKeyName() != null ? t.getKeyName() : t.getKey(),
                            t.getAlert(), t.getAlarm(), t.getAction(),
                            t.getUnit() != null ? t.getUnit() : ""));
                }
            }
        }
    }

    private void showAlarmStats() throws SdkException {
        println("\n🚨 告警统计:");
        AlarmStatistics stats = client.alarms().getStatistics();

        println("   总数:         " + stats.getTotal());
        println("   ─────────────────");
        println("   活跃未确认:   " + stats.getActiveUnack());
        println("   活跃已确认:   " + stats.getActiveAck());
        println("   已清除未确认: " + stats.getClearedUnack());
        println("   已清除已确认: " + stats.getClearedAck());
        println("   ─────────────────");
        println("   紧急(ACTION): " + stats.getCritical());
        println("   报警(ALARM):  " + stats.getMajor());
        println("   预警(ALERT):  " + stats.getWarning());
    }

    private void queryTelemetry() throws SdkException {
        println("\n💡 提示: 实体ID可以是设备、监测点或项目的完整UUID");
        println("   (从上面的列表中复制ID)\n");

        String entityId = prompt("实体ID (设备/监测点/项目)", null);
        if (entityId == null || entityId.isBlank()) {
            println("实体ID 不能为空");
            return;
        }

        String keys = prompt("遥测键名 (逗号分隔)", "temperature");
        String hours = prompt("查询最近小时数", "1");

        println("\n📊 遥测数据:");
        Map<String, List<TelemetryValue>> result = client.telemetry().list(
                TelemetryQuery.builder()
                        .entityId(entityId)
                        .keys(keys.split(","))
                        .lastHours(Integer.parseInt(hours))
                        .limit(10)
                        .orderDesc(true)
                        .build()
        );

        if (result.isEmpty()) {
            println("   (无数据)");
            return;
        }

        for (Map.Entry<String, List<TelemetryValue>> entry : result.entrySet()) {
            println("\n   " + entry.getKey() + " (" + entry.getValue().size() + " 条):");
            for (TelemetryValue v : entry.getValue()) {
                String time = TIME_FMT.format(Instant.ofEpochMilli(v.getTs()));
                println(String.format("     %s : %s", time, v.getValue()));
            }
        }
    }

    private void listActiveAlarms() throws SdkException {
        println("\n🔔 活跃未确认告警:");
        List<Alarm> alarms = client.alarms().getActiveUnacknowledged();

        if (alarms.isEmpty()) {
            println("   ✅ 暂无活跃告警");
            return;
        }

        println("   共 " + alarms.size() + " 条\n");
        for (Alarm a : alarms) {
            String time = TIME_FMT.format(Instant.ofEpochMilli(a.getStartTs()));
            println(String.format("   • [%s] %s | %s | %s",
                    a.getSeverity(), a.getType(), a.getOriginatorName(), time));
        }
    }

    // === 辅助方法 ===

    private String prompt(String message, String defaultValue) {
        if (defaultValue != null) {
            System.out.print(message + " [" + defaultValue + "]: ");
        } else {
            System.out.print(message + ": ");
        }
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }

    private String promptSecret(String message) {
        Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword(message + ": ");
            return chars != null ? new String(chars) : null;
        } else {
            // IDE 中运行时没有 Console
            System.out.print(message + ": ");
            return scanner.nextLine().trim();
        }
    }

    private void println(String msg) {
        System.out.println(msg);
    }

    private void printBanner() {
        println("");
        println("╔═══════════════════════════════════════════════════╗");
        println("║      Inteagle Cloud SDK - 交互式测试工具          ║");
        println("╚═══════════════════════════════════════════════════╝");
        println("");
    }
}
