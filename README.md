# Inteagle Cloud SDK for Java

用于 Inteagle 结构健康监测 (SHM) 云平台的 Java SDK。

## 功能特性

- **REST API 客户端** - 查询项目、设备、监测点、遥测数据、告警、告警规则
- **MQTT 订阅器** - 通过 MQTT 实时订阅数据
- **HMAC 签名认证** - 基于 AK/SK 的安全认证
- **分页查询** - 支持分页和流式查询
- **关联查询** - 设备/监测点支持关联告警规则查询

## 安装

### Maven

```xml
<dependency>
    <groupId>com.inteagle</groupId>
    <artifactId>shm-sdk</artifactId>
    <version>0.3.1</version>
</dependency>
```

## 快速开始

### 创建客户端

```java
import com.inteagle.sdk.InteagleClient;

// 创建客户端
InteagleClient client = InteagleClient.builder()
    .endpoint("https://api.shm.inteagle.com")
    .credentials("ak_xxx", "sk_xxx")
    .build();
```

### 查询项目

```java
import com.inteagle.sdk.model.MonitoringProject;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.ProjectQuery;

// 分页查询项目
PageResult<MonitoringProject> projects = client.projects().list(
    ProjectQuery.builder()
        .pageSize(20)
        .build()
);

for (MonitoringProject project : projects) {
    System.out.println(project.getName());
}
```

### 查询设备

```java
import com.inteagle.sdk.model.Device;
import com.inteagle.sdk.query.DeviceQuery;

// 查询设备列表（包含关联的告警规则）
PageResult<Device> devices = client.devices().list(
    DeviceQuery.builder()
        .projectId("project-id")
        .includeAlarmRules(true)
        .pageSize(20)
        .build()
);

// 获取单个设备
Device device = client.devices().get("device-id");
```

### 查询监测点

```java
import com.inteagle.sdk.model.MonitoringPoint;
import com.inteagle.sdk.query.PointQuery;

// 查询监测点列表（包含关联的告警规则）
PageResult<MonitoringPoint> points = client.points().list(
    PointQuery.builder()
        .projectId("project-id")
        .includeAlarmRules(true)
        .pageSize(20)
        .build()
);

// 按设备查询关联的监测点
PageResult<MonitoringPoint> devicePoints = client.points().list(
    PointQuery.ofDevice("device-id").build()
);
```

### 查询遥测数据

```java
import com.inteagle.sdk.model.TelemetryValue;
import com.inteagle.sdk.query.TelemetryQuery;

// 查询历史遥测数据
Map<String, List<TelemetryValue>> telemetry = client.telemetry().list(
    TelemetryQuery.builder()
        .entityId("device-id")
        .keys("temperature", "humidity")
        .lastHours(24)
        .build()
);

// 获取最新遥测
Map<String, List<TelemetryValue>> latest = client.telemetry().getLatest(
    "device-id", "temperature"
);
```

### 查询告警

```java
import com.inteagle.sdk.model.Alarm;
import com.inteagle.sdk.model.AlarmStatistics;
import com.inteagle.sdk.query.AlarmQuery;

// 查询告警列表
PageResult<Alarm> alarms = client.alarms().list(
    AlarmQuery.builder()
        .active(true)
        .pageSize(50)
        .build()
);

// 获取告警统计
AlarmStatistics stats = client.alarms().getStatistics();
```

### 查询告警规则

```java
import com.inteagle.sdk.model.AlarmRule;
import com.inteagle.sdk.query.AlarmRuleQuery;

// 查询项目的所有告警规则
PageResult<AlarmRule> rules = client.alarmRules().list(
    AlarmRuleQuery.ofProject("project-id").build()
);

// 按实体类型过滤（设备/监测点）
PageResult<AlarmRule> deviceRules = client.alarmRules().list(
    AlarmRuleQuery.builder()
        .projectId("project-id")
        .entityType("DEVICE")
        .build()
);

// 查询特定设备的告警规则
PageResult<AlarmRule> rules = client.alarmRules().list(
    AlarmRuleQuery.ofEntity("project-id", "DEVICE", "device-id").build()
);

// 获取单个规则详情
AlarmRule rule = client.alarmRules().get("project-id", "rule-id");

// 遍历规则的三级阈值 (3A模式)
for (AlarmRule.ThresholdRule threshold : rule.getThresholds()) {
    System.out.println("指标: " + threshold.getKeyName());
    System.out.println("预警阈值: " + threshold.getAlert() + " → " + threshold.getEffectiveAlertSeverity());
    System.out.println("报警阈值: " + threshold.getAlarm() + " → " + threshold.getEffectiveAlarmSeverity());
    System.out.println("紧急阈值: " + threshold.getAction() + " → " + threshold.getEffectiveActionSeverity());
}
```

### MQTT 实时订阅

```java
// 带 MQTT 的客户端
InteagleClient client = InteagleClient.builder()
    .endpoint("https://api.shm.inteagle.com")
    .credentials("ak_xxx", "sk_xxx")
    .mqtt("broker.shm.inteagle.com", "customer-id")
    .build();

// 连接并订阅
client.mqtt().connect();
client.mqtt().subscribeProject("project-id", message -> {
    System.out.println("类型: " + message.getType());
    System.out.println("数据: " + message.getPayload());
});
```

### 资源管理

SDK 实现了 `Closeable` 接口，建议使用 try-with-resources：

```java
try (InteagleClient client = InteagleClient.builder()
        .endpoint("https://api.shm.inteagle.com")
        .credentials("ak_xxx", "sk_xxx")
        .build()) {

    // 使用 client...

} // 自动关闭资源
```

## API 参考

### InteagleClient

| 方法 | 说明 |
|------|------|
| `projects()` | 获取项目 API |
| `devices()` | 获取设备 API |
| `points()` | 获取监测点 API |
| `telemetry()` | 获取遥测 API |
| `alarms()` | 获取告警 API |
| `alarmRules()` | 获取告警规则 API |
| `mqtt()` | 获取 MQTT 订阅器 |

### 告警规则 (3A 阈值模式)

告警规则使用三级阈值模式 (3A)，每个监测指标可配置三个级别：

| 级别 | 字段 | 默认严重级别 |
|------|------|-------------|
| 预警 | `alert` | WARNING |
| 报警 | `alarm` | MAJOR |
| 紧急 | `action` | CRITICAL |

规则作用范围：
- **项目级** (`entityId` 为空): 应用于该类型的所有实体
- **实体级** (`entityId` 有值): 仅应用于特定设备/监测点

### 查询构建器

所有查询支持 Builder 模式：

```java
// 通用分页参数
.page(0)           // 页码（从 0 开始）
.pageSize(20)      // 每页大小
.page(0, 20)       // 同时设置

// 设备查询
DeviceQuery.builder()
    .projectId("project-id")
    .name("传感器")
    .type("sensor")
    .includeAlarmRules(true)
    .build();

// 监测点查询
PointQuery.builder()
    .projectId("project-id")
    .deviceId("device-id")
    .includeAlarmRules(true)
    .build();

// 告警规则查询
AlarmRuleQuery.builder()
    .projectId("project-id")
    .entityType("DEVICE")
    .enabled(true)
    .build();
```

## 系统要求

- Java 17 或更高版本
- Maven 3.6+

## 构建

```bash
# 构建
mvn clean package

# 运行测试
mvn test

# 安装到本地仓库
mvn install
```

## 环境变量

```bash
export INTEAGLE_API_ENDPOINT=https://api.shm.inteagle.com
export INTEAGLE_ACCESS_KEY=ak_xxx
export INTEAGLE_SECRET_KEY=sk_xxx
```

## 许可证

Apache License 2.0

## 支持

- 问题反馈: https://github.com/inteagle-vision/shm-sdk/issues
