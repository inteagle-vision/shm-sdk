/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.inteagle.sdk.InteagleClient;
import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.model.*;
import com.inteagle.sdk.model.attr.AttrTypes;
import com.inteagle.sdk.model.attr.Target;
import com.inteagle.sdk.query.*;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.inteagle.sdk.mqtt.MqttSubscriber;
import com.inteagle.sdk.mqtt.data.TelemetryData;
import com.inteagle.sdk.mqtt.data.AlarmData;

/**
 * Inteagle Cloud SDK CLI
 * <p>
 * 运行方式:
 * <pre>
 * # 编译
 * mvn test-compile
 *
 * # 查看帮助
 * java -cp target/test-classes:target/classes:... com.inteagle.sdk.integration.SdkCli --help
 *
 * # 查询设备
 * java -cp ... com.inteagle.sdk.integration.SdkCli devices list --ak=xxx --sk=xxx
 *
 * # 查询遥测
 * java -cp ... com.inteagle.sdk.integration.SdkCli telemetry query --entity=xxx --keys=T_01.dx
 * </pre>
 */
@Command(
    name = "inteagle",
    mixinStandardHelpOptions = true,
    version = "Inteagle SDK CLI 0.3.2",
    description = "Inteagle Cloud Platform SDK 命令行工具",
    subcommands = {
        SdkCli.DevicesCommand.class,
        SdkCli.ProjectsCommand.class,
        SdkCli.PointsCommand.class,
        SdkCli.TelemetryCommand.class,
        SdkCli.AlarmsCommand.class,
        SdkCli.AlarmRulesCommand.class,
        SdkCli.MqttCommand.class,
        SdkCli.InteractiveCommand.class,
        CommandLine.HelpCommand.class
    }
)
public class SdkCli implements Runnable {

    // ==================== 全局选项 ====================

    @Option(names = {"-e", "--endpoint"},
            description = "API 端点 URL (环境变量: INTEAGLE_ENDPOINT)",
            scope = ScopeType.INHERIT)
    String endpoint;

    @Option(names = {"--ak", "--access-key"},
            description = "Access Key (环境变量: INTEAGLE_ACCESS_KEY)",
            scope = ScopeType.INHERIT)
    String accessKey;

    @Option(names = {"--sk", "--secret-key"},
            description = "Secret Key (环境变量: INTEAGLE_SECRET_KEY)",
            scope = ScopeType.INHERIT)
    String secretKey;

    @Option(names = {"--mqtt-host"},
            description = "MQTT Broker 地址 (环境变量: MQTT_BROKER)",
            scope = ScopeType.INHERIT)
    String mqttHost;

    @Option(names = {"--mqtt-port"},
            description = "MQTT Broker 端口 (环境变量: MQTT_PORT)",
            scope = ScopeType.INHERIT)
    Integer mqttPort;

    @Option(names = {"--mqtt-tls"},
            description = "使用 TLS 连接 MQTT",
            scope = ScopeType.INHERIT)
    boolean mqttUseTls;

    // 从环境变量获取默认值
    String getEndpoint() {
        if (endpoint != null && !endpoint.isBlank()) return endpoint;
        String env = System.getenv("INTEAGLE_ENDPOINT");
        return (env != null && !env.isBlank()) ? env : "https://api.shm.inteagle.com";
    }

    String getAccessKey() {
        if (accessKey != null && !accessKey.isBlank()) return accessKey;
        return System.getenv("INTEAGLE_ACCESS_KEY");
    }

    String getSecretKey() {
        if (secretKey != null && !secretKey.isBlank()) return secretKey;
        return System.getenv("INTEAGLE_SECRET_KEY");
    }

    String getMqttHost() {
        if (mqttHost != null && !mqttHost.isBlank()) return mqttHost;
        String env = System.getenv("MQTT_BROKER");
        return (env != null && !env.isBlank()) ? env : "broker.shm.inteagle.com";
    }

    int getMqttPort() {
        if (mqttPort != null) return mqttPort;
        String env = System.getenv("MQTT_PORT");
        if (env != null && !env.isBlank()) {
            try { return Integer.parseInt(env); } catch (NumberFormatException ignored) {}
        }
        return mqttUseTls ? 8883 : 21883;
    }

    boolean validateCredentials() {
        String ak = getAccessKey();
        String sk = getSecretKey();
        if (ak == null || ak.isBlank() || sk == null || sk.isBlank()) {
            printError("缺少凭证! 请通过以下方式提供:");
            System.out.println("  1. 命令行: --ak=xxx --sk=xxx");
            System.out.println("  2. 环境变量: INTEAGLE_ACCESS_KEY, INTEAGLE_SECRET_KEY");
            return false;
        }
        return true;
    }

    @Option(names = {"-t", "--timeout"},
            description = "请求超时时间(秒)",
            defaultValue = "30",
            scope = ScopeType.INHERIT)
    int timeout;

    @Option(names = {"--no-color"},
            description = "禁用彩色输出",
            scope = ScopeType.INHERIT)
    boolean noColor;

    // ==================== 格式化输出工具 ====================

    static final DateTimeFormatter TIME_FMT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    // ANSI 颜色码
    static final String RESET = "\u001B[0m";
    static final String BOLD = "\u001B[1m";
    static final String GREEN = "\u001B[32m";
    static final String YELLOW = "\u001B[33m";
    static final String BLUE = "\u001B[34m";
    static final String CYAN = "\u001B[36m";
    static final String RED = "\u001B[31m";
    static final String DIM = "\u001B[2m";

    String green(String s) { return noColor ? s : GREEN + s + RESET; }
    String yellow(String s) { return noColor ? s : YELLOW + s + RESET; }
    String blue(String s) { return noColor ? s : BLUE + s + RESET; }
    String cyan(String s) { return noColor ? s : CYAN + s + RESET; }
    String red(String s) { return noColor ? s : RED + s + RESET; }
    String bold(String s) { return noColor ? s : BOLD + s + RESET; }
    String dim(String s) { return noColor ? s : DIM + s + RESET; }

    void printHeader(String title) {
        System.out.println();
        System.out.println(bold("━".repeat(60)));
        System.out.println(bold("  " + title));
        System.out.println(bold("━".repeat(60)));
    }

    void printSuccess(String msg) {
        System.out.println(green("✓ ") + msg);
    }

    void printError(String msg) {
        System.out.println(red("✗ ") + msg);
    }

    void printInfo(String msg) {
        System.out.println(blue("ℹ ") + msg);
    }

    void printWarning(String msg) {
        System.out.println(yellow("⚠ ") + msg);
    }

    void printItem(String label, Object value) {
        System.out.printf("  %s: %s%n", dim(label), value != null ? value : "-");
    }

    void printListItem(String name, String... details) {
        System.out.println();
        System.out.println("  " + cyan("●") + " " + bold(name));
        for (String detail : details) {
            System.out.println("    " + dim(detail));
        }
    }

    void printTable(String[] headers, List<String[]> rows) {
        if (rows.isEmpty()) {
            System.out.println(dim("  (无数据)"));
            return;
        }

        // 计算列宽
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < row.length && i < widths.length; i++) {
                widths[i] = Math.max(widths[i], row[i] != null ? row[i].length() : 0);
            }
        }

        // 打印表头
        StringBuilder headerLine = new StringBuilder("  ");
        StringBuilder separator = new StringBuilder("  ");
        for (int i = 0; i < headers.length; i++) {
            headerLine.append(String.format("%-" + (widths[i] + 2) + "s", headers[i]));
            separator.append("─".repeat(widths[i] + 2));
        }
        System.out.println(bold(headerLine.toString()));
        System.out.println(dim(separator.toString()));

        // 打印数据行
        for (String[] row : rows) {
            StringBuilder line = new StringBuilder("  ");
            for (int i = 0; i < headers.length; i++) {
                String value = (i < row.length && row[i] != null) ? row[i] : "-";
                line.append(String.format("%-" + (widths[i] + 2) + "s", value));
            }
            System.out.println(line);
        }
    }

    // ==================== 客户端管理 ====================

    InteagleClient createClient() {
        if (!validateCredentials()) {
            throw new IllegalStateException("凭证未配置");
        }
        return InteagleClient.builder()
                .endpoint(getEndpoint())
                .credentials(getAccessKey(), getSecretKey())
                .timeout(Duration.ofSeconds(timeout))
                .build();
    }

    @Override
    public void run() {
        System.out.println();
        System.out.println(bold("╔════════════════════════════════════════════════════════╗"));
        System.out.println(bold("║     ") + cyan("Inteagle Cloud SDK CLI") + bold("                           ║"));
        System.out.println(bold("║     ") + dim("物联网监测平台命令行工具 v0.3.1") + bold("              ║"));
        System.out.println(bold("╚════════════════════════════════════════════════════════╝"));
        System.out.println();
        System.out.println("使用 " + cyan("inteagle --help") + " 查看可用命令");
        System.out.println("使用 " + cyan("inteagle <command> --help") + " 查看子命令帮助");
        System.out.println();
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new SdkCli())
                .setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO))
                .setExecutionExceptionHandler((ex, commandLine, parseResult) -> {
                    if (ex instanceof IllegalStateException) {
                        // 凭证未配置，错误已打印
                        return 1;
                    }
                    System.err.println(commandLine.getColorScheme().errorText(ex.getMessage()));
                    return 1;
                });

        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }

    // ==================== 设备命令 ====================

    @Command(name = "devices", aliases = {"d", "device"},
             description = "设备管理命令",
             subcommands = {CommandLine.HelpCommand.class})
    static class DevicesCommand implements Runnable {

        @ParentCommand
        SdkCli parent;

        @Override
        public void run() {
            System.out.println("使用 " + parent.cyan("devices list") + " 或 " +
                             parent.cyan("devices get <id>") + " 查看设备");
        }

        @Command(name = "list", aliases = {"ls"}, description = "列出设备")
        void list(
                @Option(names = {"-p", "--project"}, description = "项目ID") String projectId,
                @Option(names = {"-n", "--name"}, description = "名称过滤") String name,
                @Option(names = {"--type"}, description = "设备类型") String type,
                @Option(names = {"--include-attrs"}, description = "包含属性") boolean includeAttrs,
                @Option(names = {"--include-points"}, description = "包含监测点") boolean includePoints,
                @Option(names = {"-l", "--limit"}, description = "数量限制", defaultValue = "20") int limit
        ) {
            parent.printHeader("设备列表");

            try (InteagleClient client = parent.createClient()) {
                DeviceQuery query = DeviceQuery.builder()
                        .pageSize(limit)
                        .includeAttributes(includeAttrs)
                        .includeMonitoringPoints(includePoints);

                if (projectId != null) query.projectId(projectId);
                if (name != null) query.name(name);
                if (type != null) query.type(type);

                PageResult<Device> result = client.devices().list(query.build());
                parent.printInfo("共 " + result.getTotal() + " 个设备");

                for (Device d : result) {
                    String status = d.isOnline() ? parent.green("在线") : parent.dim("离线");
                    parent.printListItem(
                        d.getName(),
                        "ID: " + d.getDeviceId(),
                        "类型: " + (d.getType() != null ? d.getType() : "-") + " | 状态: " + status
                    );

                    if (includeAttrs && d.getAttributes() != null && !d.getAttributes().isEmpty()) {
                        System.out.println("    " + parent.dim("属性: " + d.getAttributes().keySet()));
                    }

                    if (includePoints && d.getMonitoringPoints() != null) {
                        System.out.println("    " + parent.dim("监测点: " + d.getMonitoringPoints().size() + " 个"));
                    }
                }

                parent.printSuccess("查询完成");
            } catch (SdkException e) {
                parent.printError(e.getErrorCode() + ": " + e.getMessage());
            }
        }

        @Command(name = "get", description = "获取设备详情")
        void get(
                @Parameters(index = "0", description = "设备ID") String deviceId,
                @Option(names = {"--include-attrs"}, description = "包含属性", defaultValue = "true") boolean includeAttrs,
                @Option(names = {"-v", "--verbose"}, description = "显示完整属性值（不截断）") boolean verbose
        ) {
            parent.printHeader("设备详情");

            try (InteagleClient client = parent.createClient()) {
                Device device = client.devices().get(deviceId);

                parent.printItem("名称", device.getName());
                parent.printItem("ID", device.getDeviceId());
                parent.printItem("类型", device.getType());
                parent.printItem("型号", device.getModel());
                parent.printItem("状态", device.isOnline() ? parent.green("在线") : parent.dim("离线"));
                parent.printItem("创建时间", device.getCreatedAt());

                if (includeAttrs && device.getAttributes() != null && !device.getAttributes().isEmpty()) {
                    System.out.println();
                    System.out.println("  " + parent.bold("属性 (" + device.getAttributes().size() + " 个):"));

                    // 使用类型安全 API 显示 targets
                    List<Target> targets = device.getAttribute("targets", AttrTypes.TARGET_LIST);
                    if (targets != null && !targets.isEmpty()) {
                        System.out.println("    " + parent.cyan("targets") + ": " + targets.size() + " 个目标点");
                        for (Target t : targets) {
                            String basePointStr = Boolean.TRUE.equals(t.getBasePoint()) ? parent.green("基准点") : "";
                            System.out.println("      " + parent.bold(t.getTargetId())
                                + " [" + t.getTargetModel() + "] " + basePointStr);
                            if (verbose && t.getRoi() != null) {
                                System.out.println("        ROI: " + parent.dim(
                                    "x=" + t.getRoi().getX() + ", y=" + t.getRoi().getY()
                                    + ", " + t.getRoi().getWidth() + "x" + t.getRoi().getHeight()));
                            }
                            if (verbose && t.getRefPoint() != null) {
                                System.out.println("        RefPoint: " + parent.dim(
                                    "x=" + String.format("%.2f", t.getRefPoint().getX())
                                    + ", y=" + String.format("%.2f", t.getRefPoint().getY())
                                    + ", r=" + String.format("%.2f", t.getRefPoint().getR())));
                            }
                        }
                    }

                    // 其他属性
                    for (Map.Entry<String, Object> entry : device.getAttributes().entrySet()) {
                        if (!"targets".equals(entry.getKey())) {
                            printAttribute(entry.getKey(), entry.getValue(), verbose);
                        }
                    }
                }

                parent.printSuccess("查询完成");
            } catch (SdkException e) {
                parent.printError(e.getErrorCode() + ": " + e.getMessage());
            }
        }

        private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);

        private void printAttribute(String key, Object value, boolean verbose) {
            if (value == null) {
                System.out.println("    " + parent.cyan(key) + ": -");
                return;
            }

            // 检查是否为复杂对象（Map、List 或 JSON 字符串）
            boolean isComplex = value instanceof Map || value instanceof List;
            boolean isJsonString = false;
            String strValue = value.toString().trim();
            if (value instanceof String && (strValue.startsWith("[") || strValue.startsWith("{"))) {
                isJsonString = true;
            }

            if ((isComplex || isJsonString) && verbose) {
                // verbose 模式：格式化输出 JSON
                try {
                    String json;
                    if (isJsonString) {
                        // 解析并重新格式化 JSON 字符串
                        Object parsed = JSON_MAPPER.readValue(strValue, Object.class);
                        json = JSON_MAPPER.writeValueAsString(parsed);
                    } else {
                        json = JSON_MAPPER.writeValueAsString(value);
                    }
                    System.out.println("    " + parent.cyan(key) + ":");
                    // 缩进每一行
                    for (String line : json.split("\n")) {
                        System.out.println("      " + parent.dim(line));
                    }
                } catch (Exception e) {
                    System.out.println("    " + parent.cyan(key) + ": " + value);
                }
            } else if (isComplex || isJsonString) {
                // 非 verbose：截断显示
                String display = strValue.length() > 80 ? strValue.substring(0, 77) + "..." : strValue;
                System.out.println("    " + parent.cyan(key) + ": " + display);
            } else {
                // 简单值直接显示
                if (!verbose && strValue.length() > 80) {
                    strValue = strValue.substring(0, 77) + "...";
                }
                System.out.println("    " + parent.cyan(key) + ": " + strValue);
            }
        }
    }

    // ==================== 项目命令 ====================

    @Command(name = "projects", aliases = {"p", "project"},
             description = "项目管理命令",
             subcommands = {CommandLine.HelpCommand.class})
    static class ProjectsCommand implements Runnable {

        @ParentCommand
        SdkCli parent;

        @Override
        public void run() {
            list(20);
        }

        @Command(name = "list", aliases = {"ls"}, description = "列出项目")
        void list(
                @Option(names = {"-l", "--limit"}, description = "数量限制", defaultValue = "20") int limit
        ) {
            parent.printHeader("项目列表");

            try (InteagleClient client = parent.createClient()) {
                PageResult<MonitoringProject> result = client.projects().list(
                        ProjectQuery.builder().pageSize(limit).build()
                );
                parent.printInfo("共 " + result.getTotal() + " 个项目");

                for (MonitoringProject p : result) {
                    String status = Boolean.TRUE.equals(p.getActive())
                            ? parent.green("活跃") : parent.dim("非活跃");
                    parent.printListItem(
                        p.getName(),
                        "ID: " + p.getId(),
                        "状态: " + status
                    );
                }

                parent.printSuccess("查询完成");
            } catch (SdkException e) {
                parent.printError(e.getErrorCode() + ": " + e.getMessage());
            }
        }
    }

    // ==================== 监测点命令 ====================

    @Command(name = "points", aliases = {"pt", "point"},
             description = "监测点管理命令",
             subcommands = {CommandLine.HelpCommand.class})
    static class PointsCommand implements Runnable {

        @ParentCommand
        SdkCli parent;

        @Override
        public void run() {
            list(null, null, 20);
        }

        @Command(name = "list", aliases = {"ls"}, description = "列出监测点")
        void list(
                @Option(names = {"-p", "--project"}, description = "项目ID") String projectId,
                @Option(names = {"-d", "--device"}, description = "设备ID") String deviceId,
                @Option(names = {"-l", "--limit"}, description = "数量限制", defaultValue = "20") int limit
        ) {
            parent.printHeader("监测点列表");

            try (InteagleClient client = parent.createClient()) {
                PointQuery query = PointQuery.builder().pageSize(limit);
                if (projectId != null) query.projectId(projectId);
                if (deviceId != null) query.deviceId(deviceId);

                PageResult<MonitoringPoint> result = client.points().list(query.build());
                parent.printInfo("共 " + result.getTotal() + " 个监测点");

                for (MonitoringPoint p : result) {
                    parent.printListItem(
                        p.getName(),
                        "ID: " + p.getId(),
                        "类型: " + (p.getType() != null ? p.getType() : "-")
                    );
                }

                parent.printSuccess("查询完成");
            } catch (SdkException e) {
                parent.printError(e.getErrorCode() + ": " + e.getMessage());
            }
        }
    }

    // ==================== 遥测命令 ====================

    @Command(name = "telemetry", aliases = {"ts", "tele"},
             description = "遥测数据命令",
             subcommands = {CommandLine.HelpCommand.class})
    static class TelemetryCommand implements Runnable {

        @ParentCommand
        SdkCli parent;

        @Override
        public void run() {
            System.out.println("使用 " + parent.cyan("telemetry query --entity=xxx --keys=key1,key2") + " 查询遥测数据");
        }

        @Command(name = "query", aliases = {"q"}, description = "查询遥测数据")
        void query(
                @Option(names = {"--entity", "-E"}, description = "实体ID", required = true) String entityId,
                @Option(names = {"--keys", "-k"}, description = "键名(逗号分隔)", required = true) String keys,
                @Option(names = {"--hours", "-H"}, description = "查询最近小时数", defaultValue = "1") int hours,
                @Option(names = {"-l", "--limit"}, description = "每个键的数量限制", defaultValue = "10") int limit
        ) {
            parent.printHeader("遥测数据查询");
            parent.printItem("实体ID", entityId);
            parent.printItem("键名", keys);
            parent.printItem("时间范围", "最近 " + hours + " 小时");

            try (InteagleClient client = parent.createClient()) {
                Map<String, List<TelemetryValue>> result = client.telemetry().list(
                        TelemetryQuery.builder()
                                .entityId(entityId)
                                .keys(keys.split(","))
                                .lastHours(hours)
                                .limit(limit)
                                .orderDesc(true)
                                .build()
                );

                if (result.isEmpty()) {
                    parent.printWarning("未查询到数据");
                    return;
                }

                for (Map.Entry<String, List<TelemetryValue>> entry : result.entrySet()) {
                    System.out.println();
                    System.out.println("  " + parent.bold(entry.getKey()) +
                                     parent.dim(" (" + entry.getValue().size() + " 条)"));
                    System.out.println("  " + parent.dim("─".repeat(40)));

                    for (TelemetryValue v : entry.getValue()) {
                        String time = TIME_FMT.format(Instant.ofEpochMilli(v.getTs()));
                        System.out.printf("  %s  %s%n",
                            parent.dim(time),
                            parent.cyan(String.valueOf(v.getValue())));
                    }
                }

                parent.printSuccess("查询完成");
            } catch (SdkException e) {
                parent.printError(e.getErrorCode() + ": " + e.getMessage());
            }
        }

        @Command(name = "latest", description = "查询最新遥测数据")
        void latest(
                @Option(names = {"--entity", "-E"}, description = "实体ID", required = true) String entityId,
                @Option(names = {"--keys", "-k"}, description = "键名(逗号分隔)") String keys
        ) {
            parent.printHeader("最新遥测数据");
            parent.printItem("实体ID", entityId);

            try (InteagleClient client = parent.createClient()) {
                TelemetryQuery.Builder query = TelemetryQuery.builder()
                        .entityId(entityId)
                        .lastHours(1)
                        .limit(1);

                if (keys != null) {
                    query.keys(keys.split(","));
                }

                Map<String, List<TelemetryValue>> result = client.telemetry().list(query.build());

                if (result.isEmpty()) {
                    parent.printWarning("未查询到数据");
                    return;
                }

                System.out.println();
                String[] headers = {"键名", "值", "时间"};
                List<String[]> rows = new java.util.ArrayList<>();
                for (Map.Entry<String, List<TelemetryValue>> entry : result.entrySet()) {
                    if (!entry.getValue().isEmpty()) {
                        TelemetryValue v = entry.getValue().get(0);
                        rows.add(new String[]{
                            entry.getKey(),
                            String.valueOf(v.getValue()),
                            TIME_FMT.format(Instant.ofEpochMilli(v.getTs()))
                        });
                    }
                }
                parent.printTable(headers, rows);

                parent.printSuccess("查询完成");
            } catch (SdkException e) {
                parent.printError(e.getErrorCode() + ": " + e.getMessage());
            }
        }
    }

    // ==================== 告警命令 ====================

    @Command(name = "alarms", aliases = {"a", "alarm"},
             description = "告警管理命令",
             subcommands = {CommandLine.HelpCommand.class})
    static class AlarmsCommand implements Runnable {

        @ParentCommand
        SdkCli parent;

        @Override
        public void run() {
            stats();
        }

        @Command(name = "stats", description = "告警统计")
        void stats() {
            parent.printHeader("告警统计");

            try (InteagleClient client = parent.createClient()) {
                AlarmStatistics stats = client.alarms().getStatistics();

                System.out.println();
                System.out.println("  " + parent.bold("总数: " + stats.getTotal()));
                System.out.println();
                System.out.println("  " + parent.dim("按状态:"));
                parent.printItem("    活跃未确认", stats.getActiveUnack());
                parent.printItem("    活跃已确认", stats.getActiveAck());
                parent.printItem("    已清除未确认", stats.getClearedUnack());
                parent.printItem("    已清除已确认", stats.getClearedAck());
                System.out.println();
                System.out.println("  " + parent.dim("按级别:"));
                parent.printItem("    紧急 (ACTION)", parent.red(String.valueOf(stats.getCritical())));
                parent.printItem("    报警 (ALARM)", parent.yellow(String.valueOf(stats.getMajor())));
                parent.printItem("    预警 (ALERT)", parent.blue(String.valueOf(stats.getWarning())));

                parent.printSuccess("查询完成");
            } catch (SdkException e) {
                parent.printError(e.getErrorCode() + ": " + e.getMessage());
            }
        }

        @Command(name = "active", description = "查看活跃告警")
        void active(
                @Option(names = {"-l", "--limit"}, description = "数量限制", defaultValue = "20") int limit
        ) {
            parent.printHeader("活跃未确认告警");

            try (InteagleClient client = parent.createClient()) {
                PageResult<Alarm> result = client.alarms().list(
                        AlarmQuery.activeUnacknowledged().pageSize(limit).build()
                );

                if (result.isEmpty()) {
                    parent.printSuccess("暂无活跃告警");
                    return;
                }

                parent.printInfo("共 " + result.getTotal() + " 条 (显示 " + result.getData().size() + " 条)");

                for (Alarm a : result) {
                    String time = TIME_FMT.format(Instant.ofEpochMilli(a.getStartTs()));
                    String severity = formatSeverity(a.getSeverity(), parent);

                    parent.printListItem(
                        a.getType(),
                        "级别: " + severity + " | 来源: " + a.getOriginatorName(),
                        "时间: " + time
                    );
                }

                parent.printSuccess("查询完成");
            } catch (SdkException e) {
                parent.printError(e.getErrorCode() + ": " + e.getMessage());
            }
        }

        private String formatSeverity(String severity, SdkCli parent) {
            if (severity == null) return "-";
            return switch (severity.toUpperCase()) {
                case "CRITICAL", "ACTION" -> parent.red(severity);
                case "MAJOR", "ALARM" -> parent.yellow(severity);
                case "MINOR", "WARNING", "ALERT" -> parent.blue(severity);
                default -> severity;
            };
        }
    }

    // ==================== 告警规则命令 ====================

    @Command(name = "alarm-rules", aliases = {"ar", "rules"},
             description = "告警规则命令",
             subcommands = {CommandLine.HelpCommand.class})
    static class AlarmRulesCommand implements Runnable {

        @ParentCommand
        SdkCli parent;

        @Override
        public void run() {
            System.out.println("使用 " + parent.cyan("alarm-rules list --project=xxx") + " 查看告警规则");
        }

        @Command(name = "list", aliases = {"ls"}, description = "列出告警规则")
        void list(
                @Option(names = {"-p", "--project"}, description = "项目ID", required = true) String projectId,
                @Option(names = {"-l", "--limit"}, description = "数量限制", defaultValue = "20") int limit
        ) {
            parent.printHeader("告警规则列表");

            try (InteagleClient client = parent.createClient()) {
                PageResult<AlarmRule> result = client.alarmRules().list(
                        AlarmRuleQuery.ofProject(projectId).pageSize(limit).build()
                );
                parent.printInfo("共 " + result.getTotal() + " 条规则");

                for (AlarmRule rule : result) {
                    String scope = rule.isProjectLevel() ? "项目级" : "实体级";
                    String status = Boolean.TRUE.equals(rule.getEnabled())
                            ? parent.green("已启用") : parent.dim("已禁用");

                    parent.printListItem(
                        rule.getName(),
                        "ID: " + rule.getId(),
                        "范围: " + scope + " | 类型: " + rule.getEntityType() + " | " + status
                    );

                    if (rule.getThresholds() != null && !rule.getThresholds().isEmpty()) {
                        System.out.println("    " + parent.dim("阈值:"));
                        for (AlarmRule.ThresholdRule t : rule.getThresholds()) {
                            String keyDisplay = t.getKeyName() != null ? t.getKeyName() : t.getKey();
                            System.out.printf("      %s: 预警=%s 报警=%s 紧急=%s%n",
                                    parent.cyan(keyDisplay),
                                    t.getAlert(), t.getAlarm(), t.getAction());
                        }
                    }
                }

                parent.printSuccess("查询完成");
            } catch (SdkException e) {
                parent.printError(e.getErrorCode() + ": " + e.getMessage());
            }
        }
    }

    // ==================== MQTT 命令 ====================

    @Command(name = "mqtt", aliases = {"m"},
             description = "MQTT 实时订阅命令",
             subcommands = {CommandLine.HelpCommand.class})
    static class MqttCommand implements Runnable {

        @ParentCommand
        SdkCli parent;

        @Override
        public void run() {
            System.out.println("使用 " + parent.cyan("mqtt listen") + " 订阅所有数据");
            System.out.println("使用 " + parent.cyan("mqtt telemetry") + " 订阅遥测数据");
            System.out.println("使用 " + parent.cyan("mqtt alarm") + " 订阅告警数据");
            System.out.println("使用 " + parent.cyan("mqtt device <id>") + " 订阅指定设备");
        }

        @Command(name = "listen", aliases = {"l"}, description = "订阅所有数据")
        void listen(
                @Option(names = {"--duration", "-d"}, description = "监听时长(秒), 0=无限", defaultValue = "0") int duration,
                @Option(names = {"--qos"}, description = "QoS 级别 (0,1,2)", defaultValue = "1") int qos
        ) {
            parent.printHeader("MQTT 实时订阅 (所有数据)");
            subscribeWithOptions(null, null, duration, qos, false, false);
        }

        @Command(name = "telemetry", aliases = {"ts"}, description = "订阅遥测数据")
        void telemetry(
                @Option(names = {"--device", "-D"}, description = "设备ID") String deviceId,
                @Option(names = {"--project", "-p"}, description = "项目ID") String projectId,
                @Option(names = {"--keys", "-k"}, description = "过滤的键名(逗号分隔)") String keys,
                @Option(names = {"--duration", "-d"}, description = "监听时长(秒), 0=无限", defaultValue = "0") int duration,
                @Option(names = {"--qos"}, description = "QoS 级别 (0,1,2)", defaultValue = "1") int qos
        ) {
            parent.printHeader("MQTT 实时订阅 (遥测数据)");
            subscribeWithOptions(deviceId, projectId, duration, qos, true, false);
        }

        @Command(name = "alarm", aliases = {"a"}, description = "订阅告警数据")
        void alarm(
                @Option(names = {"--device", "-D"}, description = "设备ID") String deviceId,
                @Option(names = {"--project", "-p"}, description = "项目ID") String projectId,
                @Option(names = {"--duration", "-d"}, description = "监听时长(秒), 0=无限", defaultValue = "0") int duration,
                @Option(names = {"--qos"}, description = "QoS 级别 (0,1,2)", defaultValue = "1") int qos
        ) {
            parent.printHeader("MQTT 实时订阅 (告警数据)");
            subscribeWithOptions(deviceId, projectId, duration, qos, false, true);
        }

        @Command(name = "device", aliases = {"d"}, description = "订阅指定设备的所有数据")
        void device(
                @Parameters(index = "0", description = "设备ID") String deviceId,
                @Option(names = {"--duration", "-d"}, description = "监听时长(秒), 0=无限", defaultValue = "0") int duration,
                @Option(names = {"--qos"}, description = "QoS 级别 (0,1,2)", defaultValue = "1") int qos
        ) {
            parent.printHeader("MQTT 实时订阅 (设备: " + deviceId + ")");
            subscribeWithOptions(deviceId, null, duration, qos, false, false);
        }

        private void subscribeWithOptions(String deviceId, String projectId, int duration,
                                          int qos, boolean telemetryOnly, boolean alarmOnly) {
            if (!parent.validateCredentials()) return;

            String brokerHost = parent.getMqttHost();
            int brokerPort = parent.getMqttPort();

            parent.printInfo("Broker: " + brokerHost + ":" + brokerPort + (parent.mqttUseTls ? " (TLS)" : ""));
            parent.printInfo("QoS: " + qos);
            if (duration > 0) {
                parent.printInfo("持续时间: " + duration + " 秒");
            } else {
                parent.printInfo("持续时间: 无限 (Ctrl+C 退出)");
            }

            try (InteagleClient client = parent.createClient()) {
                // 获取 customerId
                CredentialInfo info = client.me();
                String customerId = info.getCustomerId();
                parent.printInfo("Customer ID: " + customerId);

                // 创建 MQTT Subscriber
                MqttSubscriber subscriber = new MqttSubscriber(
                        brokerHost, brokerPort,
                        parent.getAccessKey(), parent.getSecretKey(),
                        customerId, parent.mqttUseTls
                );

                subscriber.setQosLevel(qos);

                // 设置连接事件监听器
                subscriber.setConnectionEventListener(event -> {
                    switch (event.getType()) {
                        case CONNECTED -> parent.printSuccess("MQTT 连接成功");
                        case DISCONNECTED -> parent.printWarning("MQTT 连接断开: " + event.getMessage());
                        case RECONNECTED -> parent.printInfo("MQTT 重连成功 (第" + event.getReconnectCount() + "次)");
                    }
                });

                // 连接
                parent.printInfo("正在连接 MQTT Broker...");
                subscriber.connect();

                AtomicInteger messageCount = new AtomicInteger(0);
                CountDownLatch latch = new CountDownLatch(1);

                // 设置关闭钩子
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        subscriber.disconnect();
                        parent.printInfo("\n已断开连接, 共收到 " + messageCount.get() + " 条消息");
                    } catch (Exception e) {
                        // ignore
                    }
                    latch.countDown();
                }));

                System.out.println();
                parent.printSuccess("已连接，等待消息... (Ctrl+C 退出)");
                System.out.println(parent.dim("─".repeat(60)));

                // 订阅
                if (telemetryOnly) {
                    subscribeTelemetry(subscriber, deviceId, projectId, messageCount);
                } else if (alarmOnly) {
                    subscribeAlarm(subscriber, deviceId, projectId, messageCount);
                } else {
                    subscribeAll(subscriber, deviceId, projectId, messageCount);
                }

                // 等待
                if (duration > 0) {
                    Thread.sleep(duration * 1000L);
                    subscriber.disconnect();
                    parent.printInfo("监听结束, 共收到 " + messageCount.get() + " 条消息");
                } else {
                    // 无限等待
                    latch.await();
                }

            } catch (Exception e) {
                parent.printError("MQTT 错误: " + e.getMessage());
            }
        }

        private void subscribeTelemetry(MqttSubscriber subscriber, String deviceId, String projectId,
                                         AtomicInteger messageCount) throws Exception {
            if (deviceId != null) {
                subscriber.subscribeDeviceTelemetry(deviceId, data -> {
                    printTelemetryData(data, messageCount.incrementAndGet());
                });
                parent.printInfo("已订阅设备遥测: " + deviceId);
            } else if (projectId != null) {
                subscriber.subscribeProjectTelemetry(projectId, data -> {
                    printTelemetryData(data, messageCount.incrementAndGet());
                });
                parent.printInfo("已订阅项目遥测: " + projectId);
            } else {
                subscriber.subscribeTelemetry(data -> {
                    printTelemetryData(data, messageCount.incrementAndGet());
                });
                parent.printInfo("已订阅所有遥测数据");
            }
        }

        private void subscribeAlarm(MqttSubscriber subscriber, String deviceId, String projectId,
                                     AtomicInteger messageCount) throws Exception {
            if (deviceId != null) {
                subscriber.subscribeDeviceAlarm(deviceId, data -> {
                    printAlarmData(data, messageCount.incrementAndGet());
                });
                parent.printInfo("已订阅设备告警: " + deviceId);
            } else if (projectId != null) {
                subscriber.subscribeProjectAlarm(projectId, data -> {
                    printAlarmData(data, messageCount.incrementAndGet());
                });
                parent.printInfo("已订阅项目告警: " + projectId);
            } else {
                subscriber.subscribeAlarm(data -> {
                    printAlarmData(data, messageCount.incrementAndGet());
                });
                parent.printInfo("已订阅所有告警数据");
            }
        }

        private void subscribeAll(MqttSubscriber subscriber, String deviceId, String projectId,
                                   AtomicInteger messageCount) throws Exception {
            if (deviceId != null) {
                // 订阅单个设备的所有数据
                subscriber.subscribeDeviceTelemetry(deviceId, data -> {
                    printTelemetryData(data, messageCount.incrementAndGet());
                });
                subscriber.subscribeDeviceAlarm(deviceId, data -> {
                    printAlarmData(data, messageCount.incrementAndGet());
                });
                parent.printInfo("已订阅设备: " + deviceId);
            } else {
                // 订阅所有数据
                subscriber.subscribeAll(
                    data -> printTelemetryData(data, messageCount.incrementAndGet()),
                    data -> printAlarmData(data, messageCount.incrementAndGet()),
                    null,  // event handler
                    null   // image handler
                );
                parent.printInfo("已订阅所有数据");
            }
        }

        private void printTelemetryData(TelemetryData data, int count) {
            String time = TIME_FMT.format(Instant.ofEpochMilli(data.getTimestamp()));
            StringBuilder sb = new StringBuilder();
            sb.append(parent.dim("[" + count + "] "));
            sb.append(parent.cyan("遥测"));
            sb.append(" | ").append(parent.dim(time));
            sb.append(" | ").append(parent.bold(data.getDeviceId() != null ? data.getDeviceId() : "-"));
            sb.append("\n    ");

            Map<String, Object> values = data.getValues();
            int i = 0;
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                if (i > 0) sb.append(", ");
                sb.append(parent.yellow(entry.getKey())).append("=").append(entry.getValue());
                i++;
                if (i >= 5 && values.size() > 5) {
                    sb.append(" ... (+" + (values.size() - 5) + " more)");
                    break;
                }
            }

            System.out.println(sb);
        }

        private void printAlarmData(AlarmData data, int count) {
            String time = TIME_FMT.format(Instant.ofEpochMilli(data.getTimestamp()));
            AlarmData.AlarmSeverity severity = data.getSeverity();
            String severityStr = severity != null ? severity.name() : "UNKNOWN";
            String severityColored = switch (severity != null ? severity : AlarmData.AlarmSeverity.UNKNOWN) {
                case ACTION -> parent.red(severityStr);
                case ALARM -> parent.yellow(severityStr);
                case ALERT -> parent.blue(severityStr);
                default -> parent.dim(severityStr);
            };

            System.out.println(
                parent.dim("[" + count + "] ") +
                parent.red("告警") +
                " | " + parent.dim(time) +
                " | " + severityColored +
                " | " + parent.bold(data.getAlarmType() != null ? data.getAlarmType() : "-") +
                " | 来源: " + (data.getOriginatorId() != null ? data.getOriginatorId() : "-")
            );
        }
    }

    // ==================== 交互式命令 ====================

    @Command(name = "interactive", aliases = {"i", "shell"},
             description = "进入交互式模式")
    static class InteractiveCommand implements Runnable {

        @ParentCommand
        SdkCli parent;

        private MqttSubscriptionManager mqttManager;

        @Override
        public void run() {
            if (!parent.validateCredentials()) {
                return;
            }

            parent.printHeader("交互式模式");
            parent.printInfo("端点: " + parent.getEndpoint());
            parent.printInfo("输入命令，或输入 'help' 查看帮助，'quit' 退出");

            java.util.Scanner scanner = new java.util.Scanner(System.in);
            InteagleClient client = null;

            try {
                client = parent.createClient();
                parent.printSuccess("连接成功");

                // 创建 MQTT 订阅管理器
                mqttManager = new MqttSubscriptionManager(new MqttSubscriptionManager.OutputFormatter() {
                    @Override
                    public void printSuccess(String msg) {
                        parent.printSuccess(msg);
                    }

                    @Override
                    public void printInfo(String msg) {
                        parent.printInfo(msg);
                    }

                    @Override
                    public void printWarning(String msg) {
                        parent.printWarning(msg);
                    }

                    @Override
                    public void printError(String msg) {
                        parent.printError(msg);
                    }

                    @Override
                    public void printSeparator() {
                        System.out.println(parent.dim("─".repeat(60)));
                    }

                    @Override
                    public String bold(String text) {
                        return parent.bold(text);
                    }

                    @Override
                    public String cyan(String text) {
                        return parent.cyan(text);
                    }

                    @Override
                    public String green(String text) {
                        return parent.green(text);
                    }

                    @Override
                    public String yellow(String text) {
                        return parent.yellow(text);
                    }

                    @Override
                    public String dim(String text) {
                        return parent.dim(text);
                    }
                });

                while (true) {
                    System.out.print(parent.cyan("\ninteagle> "));
                    String line = scanner.nextLine().trim();

                    if (line.isEmpty()) continue;
                    if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("q")) {
                        break;
                    }

                    try {
                        executeInteractiveCommand(client, line);
                    } catch (Exception e) {
                        parent.printError(e.getMessage());
                    }
                }
            } catch (Exception e) {
                parent.printError("连接失败: " + e.getMessage());
            } finally {
                // 清理 MQTT 订阅
                if (mqttManager != null && mqttManager.isRunning()) {
                    mqttManager.stop();
                }
                if (client != null) client.close();
                parent.printInfo("再见!");
            }
        }

        private void executeInteractiveCommand(InteagleClient client, String line) throws SdkException {
            String[] parts = line.split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            String args = parts.length > 1 ? parts[1] : "";

            switch (cmd) {
                case "help", "h", "?" -> printInteractiveHelp();
                case "projects", "p" -> listProjects(client);
                case "devices", "d" -> listDevices(client, args);
                case "points", "pt" -> listPoints(client, args);
                case "device" -> getDevice(client, args);
                case "telemetry", "ts" -> queryTelemetry(client, args);
                case "alarms", "a" -> showAlarms(client);
                case "stats" -> showAlarmStats(client);
                case "mqtt", "m" -> executeMqttCommand(client, args);
                default -> parent.printWarning("未知命令: " + cmd + " (输入 'help' 查看帮助)");
            }
        }

        private void printInteractiveHelp() {
            System.out.println();
            System.out.println(parent.bold("可用命令:"));
            System.out.println("  " + parent.cyan("projects") + "         - 列出项目");
            System.out.println("  " + parent.cyan("devices [n]") + "      - 列出设备 (可选: 数量)");
            System.out.println("  " + parent.cyan("device <id>") + "      - 查看设备详情");
            System.out.println("  " + parent.cyan("points [n]") + "       - 列出监测点 (可选: 数量)");
            System.out.println("  " + parent.cyan("telemetry <id> <keys> [hours]") + " - 查询遥测数据");
            System.out.println("  " + parent.cyan("alarms") + "           - 查看活跃告警");
            System.out.println("  " + parent.cyan("stats") + "            - 告警统计");
            System.out.println();
            System.out.println(parent.bold("MQTT 实时订阅:"));
            System.out.println("  " + parent.cyan("mqtt start [topic]") + " - 启动 MQTT 订阅 (默认: 所有数据)");
            System.out.println("  " + parent.cyan("mqtt stop") + "        - 停止 MQTT 订阅");
            System.out.println("  " + parent.cyan("mqtt status") + "      - 查看订阅状态");
            System.out.println();
            System.out.println("  " + parent.cyan("help") + "             - 显示此帮助");
            System.out.println("  " + parent.cyan("quit") + "             - 退出");
        }

        private void listProjects(InteagleClient client) throws SdkException {
            PageResult<MonitoringProject> result = client.projects().list(
                    ProjectQuery.builder().pageSize(20).build());
            parent.printInfo("共 " + result.getTotal() + " 个项目");
            for (MonitoringProject p : result) {
                System.out.printf("  %s %s %s%n",
                        parent.cyan("●"),
                        parent.bold(p.getName()),
                        parent.dim("(" + p.getId() + ")"));
            }
        }

        private void listDevices(InteagleClient client, String args) throws SdkException {
            int limit = 10;
            try { if (!args.isEmpty()) limit = Integer.parseInt(args); } catch (NumberFormatException ignored) {}

            PageResult<Device> result = client.devices().list(
                    DeviceQuery.builder().pageSize(limit).build());
            parent.printInfo("共 " + result.getTotal() + " 个设备");
            for (Device d : result) {
                String status = d.isOnline() ? parent.green("在线") : parent.dim("离线");
                System.out.printf("  %s %s [%s] %s %s%n",
                        parent.cyan("●"),
                        parent.bold(d.getName()),
                        d.getType() != null ? d.getType() : "-",
                        status,
                        parent.dim("(" + d.getDeviceId() + ")"));
            }
        }

        private void getDevice(InteagleClient client, String args) throws SdkException {
            // 解析参数: device <id> [-v]
            String[] parts = args.split("\\s+");
            String deviceId = parts.length > 0 ? parts[0] : "";
            boolean verbose = false;
            for (String p : parts) {
                if ("-v".equals(p) || "--verbose".equals(p)) {
                    verbose = true;
                }
            }

            if (deviceId.isEmpty() || "help".equalsIgnoreCase(deviceId) || "-h".equals(deviceId)) {
                System.out.println("  用法: " + parent.cyan("device <id> [-v]") + " - 查看设备详情");
                System.out.println("  选项: " + parent.dim("-v  显示详细属性"));
                System.out.println("  示例: device ddbd2770-f508-11f0-8ccb-0d404d4711dc -v");
                return;
            }
            // 验证 UUID 格式
            if (!isValidUuid(deviceId)) {
                parent.printWarning("无效的设备ID格式，应为 UUID: " + deviceId);
                return;
            }
            Device d = client.devices().get(deviceId);
            System.out.println();
            parent.printItem("名称", d.getName());
            parent.printItem("ID", d.getDeviceId());
            parent.printItem("类型", d.getType());
            parent.printItem("状态", d.isOnline() ? parent.green("在线") : parent.dim("离线"));

            // 使用类型安全 API 显示 targets
            List<Target> targets = d.getAttribute("targets", AttrTypes.TARGET_LIST);
            if (targets != null && !targets.isEmpty()) {
                System.out.println("  " + parent.cyan("targets") + ": " + targets.size() + " 个目标点");
                for (Target t : targets) {
                    String basePointStr = Boolean.TRUE.equals(t.getBasePoint()) ? parent.green(" 基准点") : "";
                    System.out.println("    " + parent.bold(t.getTargetId()) + " [" + t.getTargetModel() + "]" + basePointStr);
                    if (verbose && t.getRoi() != null) {
                        System.out.println("      ROI: " + parent.dim(t.getRoi().getWidth() + "x" + t.getRoi().getHeight()
                            + " @ " + t.getRoi().getX() + "," + t.getRoi().getY()));
                    }
                    if (verbose && t.getRefPoint() != null) {
                        System.out.println("      RefPoint: " + parent.dim(
                            String.format("(%.1f, %.1f) r=%.1f", t.getRefPoint().getX(), t.getRefPoint().getY(), t.getRefPoint().getR())));
                    }
                }
            }

            // 其他属性
            if (d.getAttributes() != null) {
                for (Map.Entry<String, Object> entry : d.getAttributes().entrySet()) {
                    if (!"targets".equals(entry.getKey())) {
                        Object v = entry.getValue();
                        String display = String.valueOf(v);
                        if (!verbose && display.length() > 50) display = display.substring(0, 47) + "...";
                        System.out.println("  " + parent.cyan(entry.getKey()) + ": " + display);
                    }
                }
            }
        }

        private void listPoints(InteagleClient client, String args) throws SdkException {
            int limit = 10;
            try { if (!args.isEmpty()) limit = Integer.parseInt(args); } catch (NumberFormatException ignored) {}

            PageResult<MonitoringPoint> result = client.points().list(
                    PointQuery.builder().pageSize(limit).build());
            parent.printInfo("共 " + result.getTotal() + " 个监测点");
            for (MonitoringPoint p : result) {
                System.out.printf("  %s %s [%s] %s%n",
                        parent.cyan("●"),
                        parent.bold(p.getName()),
                        p.getType() != null ? p.getType() : "-",
                        parent.dim("(" + p.getId() + ")"));
            }
        }

        private void queryTelemetry(InteagleClient client, String args) throws SdkException {
            String[] parts = args.split("\\s+");
            if (parts.length < 2) {
                parent.printWarning("用法: telemetry <entityId> <keys> [hours]");
                parent.printWarning("示例: telemetry abc-123 T_01.dx,T_01.dy 24");
                return;
            }
            String entityId = parts[0];
            String keys = parts[1];
            int hours = parts.length > 2 ? Integer.parseInt(parts[2]) : 1;

            Map<String, List<TelemetryValue>> result = client.telemetry().list(
                    TelemetryQuery.builder()
                            .entityId(entityId)
                            .keys(keys.split(","))
                            .lastHours(hours)
                            .limit(5)
                            .orderDesc(true)
                            .build());

            if (result.isEmpty()) {
                parent.printWarning("未查询到数据");
                return;
            }

            for (Map.Entry<String, List<TelemetryValue>> entry : result.entrySet()) {
                System.out.println("  " + parent.bold(entry.getKey()) + ":");
                for (TelemetryValue v : entry.getValue()) {
                    String time = TIME_FMT.format(Instant.ofEpochMilli(v.getTs()));
                    System.out.printf("    %s  %s%n", parent.dim(time), parent.cyan(String.valueOf(v.getValue())));
                }
            }
        }

        private void showAlarms(InteagleClient client) throws SdkException {
            PageResult<Alarm> result = client.alarms().list(
                    AlarmQuery.activeUnacknowledged().pageSize(20).build()
            );
            if (result.isEmpty()) {
                parent.printSuccess("暂无活跃告警");
                return;
            }
            parent.printInfo("共 " + result.getTotal() + " 条活跃告警");
            for (Alarm a : result) {
                String time = TIME_FMT.format(Instant.ofEpochMilli(a.getStartTs()));
                System.out.printf("  %s [%s] %s | %s%n",
                        parent.yellow("⚠"),
                        a.getSeverity(),
                        a.getType(),
                        parent.dim(time));
            }
        }

        private void showAlarmStats(InteagleClient client) throws SdkException {
            AlarmStatistics stats = client.alarms().getStatistics();
            System.out.println();
            System.out.printf("  总数: %s | 活跃: %s | 紧急: %s | 报警: %s | 预警: %s%n",
                    parent.bold(String.valueOf(stats.getTotal())),
                    parent.yellow(String.valueOf(stats.getActiveUnack())),
                    parent.red(String.valueOf(stats.getCritical())),
                    parent.yellow(String.valueOf(stats.getMajor())),
                    parent.blue(String.valueOf(stats.getWarning())));
        }

        private boolean isValidUuid(String str) {
            if (str == null || str.length() != 36) return false;
            // UUID 格式: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
            return str.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        }

        /**
         * 执行 MQTT 命令
         */
        private void executeMqttCommand(InteagleClient client, String args) throws SdkException {
            String[] parts = args.split("\\s+", 2);
            String subCmd = parts.length > 0 ? parts[0].toLowerCase() : "";
            String subArgs = parts.length > 1 ? parts[1] : "";

            switch (subCmd) {
                case "start" -> mqttStart(client, subArgs);
                case "stop" -> mqttStop();
                case "status" -> mqttStatus();
                case "" -> mqttHelp();
                default -> {
                    parent.printWarning("未知的 mqtt 子命令: " + subCmd);
                    mqttHelp();
                }
            }
        }

        private void mqttHelp() {
            System.out.println();
            System.out.println(parent.bold("MQTT 实时订阅命令:"));
            System.out.println("  " + parent.cyan("mqtt start [topic]") + " - 启动订阅");
            System.out.println("    示例:");
            System.out.println("      mqtt start              - 订阅所有数据");
            System.out.println("      mqtt start telemetry    - 仅订阅遥测数据");
            System.out.println("      mqtt start alarm        - 仅订阅告警数据");
            System.out.println();
            System.out.println("  " + parent.cyan("mqtt stop") + "          - 停止订阅");
            System.out.println("  " + parent.cyan("mqtt status") + "        - 查看订阅状态");
            System.out.println();
            System.out.println(parent.dim("提示: 订阅后实时消息将显示在下方，输入命令不受影响"));
        }

        private void mqttStart(InteagleClient client, String topicType) throws SdkException {
            if (mqttManager.isRunning()) {
                parent.printWarning("MQTT 订阅已在运行中，请先执行 'mqtt stop'");
                return;
            }

            try {
                // 获取 customerId
                CredentialInfo info = client.me();
                String customerId = info.getCustomerId();

                // 构建订阅主题
                String topic = buildTopic(customerId, topicType);

                // 启动订阅
                mqttManager.start(
                        parent.getMqttHost(),
                        parent.getMqttPort(),
                        parent.getAccessKey(),
                        parent.getSecretKey(),
                        customerId,
                        parent.mqttUseTls,
                        topic
                );

            } catch (Exception e) {
                parent.printError("启动 MQTT 订阅失败: " + e.getMessage());
                if (e.getCause() != null) {
                    parent.printError("原因: " + e.getCause().getMessage());
                }
            }
        }

        private void mqttStop() {
            if (!mqttManager.isRunning()) {
                parent.printInfo("MQTT 订阅未运行");
                return;
            }

            mqttManager.stop();
        }

        private void mqttStatus() {
            MqttSubscriptionManager.SubscriptionStatus status = mqttManager.getStatus();

            System.out.println();
            if (status.isRunning()) {
                parent.printInfo("MQTT 订阅状态: " + parent.green("运行中"));
                System.out.println("  • 主题: " + parent.cyan(status.getTopic()));
                System.out.println("  • 消息数: " + parent.cyan(String.valueOf(status.getMessageCount())));
                System.out.println("  • 运行时长: " + parent.cyan(status.getDurationSeconds() + " 秒"));
            } else {
                parent.printInfo("MQTT 订阅状态: " + parent.dim("未运行"));
                parent.printInfo("使用 'mqtt start' 启动订阅");
            }
            System.out.println();
        }

        /**
         * 构建订阅主题
         */
        private String buildTopic(String customerId, String topicType) {
            String baseTopic = "inteagle/" + customerId;

            return switch (topicType.toLowerCase()) {
                case "telemetry", "ts", "t" -> baseTopic + "/d/+/telemetry";
                case "alarm", "alarms", "a" -> baseTopic + "/d/+/alarm";
                case "event", "events", "e" -> baseTopic + "/d/+/event";
                case "image", "images", "img" -> baseTopic + "/d/+/image";
                case "device", "d" -> baseTopic + "/d/#";
                case "project", "p" -> baseTopic + "/p/#";
                case "", "all" -> baseTopic + "/#";
                default -> {
                    // 自定义主题
                    if (topicType.startsWith("inteagle/")) {
                        yield topicType;
                    } else {
                        yield baseTopic + "/" + topicType;
                    }
                }
            };
        }
    }
}
