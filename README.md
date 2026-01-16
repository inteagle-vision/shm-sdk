# 结构健康监测平台 Java SDK

用于 Inteagle 结构健康监测 (SHM) 云平台的 Java SDK。

## 功能特性

- **REST API 客户端** - 查询设备、项目、遥测数据
- **MQTT 订阅器** - 通过 MQTT 实时订阅数据
- **统一客户端** - REST 和 MQTT 的单一入口
- **AK/SK 认证** - 基于访问密钥的安全认证

## 安装

### Maven

首先添加 JitPack 仓库：

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

然后添加依赖：

```xml
<dependency>
    <groupId>com.github.inteagle-vision</groupId>
    <artifactId>shm-sdk</artifactId>
    <version>v0.1.1</version>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.inteagle-vision:shm-sdk:v0.1.1'
}
```

## 快速开始

### 资源管理（重要）

SDK 实现了 `AutoCloseable` 接口，建议使用 try-with-resources 确保资源正确释放：

```java
try (InteagleClient client = InteagleClient.builder()
        .apiEndpoint("https://api.shm.inteagle.com")
        .mqttEndpoint("broker.shm.inteagle.com", 8883)
        .credentials(customerId, accessToken)
        .useTls(true)
        .build()) {
    
    // REST API - 查询数据
    List<Device> devices = client.rest().getDevices("project-id");
    
    // MQTT - 实时订阅
    client.mqtt().connect();
    client.mqtt().subscribeProject("project-id", message -> {
        System.out.println("类型: " + message.getType());
        System.out.println("数据: " + message.getPayload());
    });
    
} // 资源自动清理
```

### 使用访问令牌（传统方式）

```java
import com.inteagle.sdk.InteagleClient;

// 使用 AK/SK 凭证创建客户端
InteagleClient client = InteagleClient.builder()
    .apiEndpoint("https://api.shm.inteagle.com")
    .mqttEndpoint("broker.shm.inteagle.com", 8883)
    .credentials(customerId, accessToken)
    .useTls(true)
    .build();

// REST API - 查询数据
List<Device> devices = client.rest().getDevices("project-id");
Map<String, Object> telemetry = client.rest().getLatestTelemetry("device-id");

// MQTT - 实时订阅
client.mqtt().connect();
client.mqtt().subscribeProject("project-id", message -> {
    System.out.println("类型: " + message.getType());
    System.out.println("数据: " + message.getPayload());
});
```

## 消息类型

SDK 通过 MQTT 支持以下消息类型：

| 类型 | 说明 |
|------|------|
| `TELEMETRY` | 设备遥测数据（传感器、测量值） |
| `ATTRIBUTES` | 设备属性（配置、元数据） |
| `ALARM` (3A) | 告警通知（创建、更新、清除） |
| `EVENT` | 系统事件 |
| `IMAGE` | 摄像头图像数据 |

## Topic 格式

```
inteagle/{customerId}/p/{projectId}/d/{deviceId}    # 设备数据
inteagle/{customerId}/p/{projectId}/mp/{pointId}    # 监测点数据
```

## API 参考

### InteagleClient

```java
// Builder 模式
InteagleClient.builder()
    .apiEndpoint(String url)           // REST API 端点
    .mqttEndpoint(String host, int port)  // MQTT 代理
    .credentials(String customerId, String accessToken)
    .useTls(boolean)                   // 启用 TLS（默认: true）
    .build();
```

### RestClient

```java
client.rest().getDevices(String projectId);           // 获取项目下的设备列表
client.rest().getDevice(String deviceId);             // 获取设备详情
client.rest().getLatestTelemetry(String deviceId);    // 获取最新遥测数据
client.rest().getTelemetryHistory(...);               // 获取历史遥测数据
client.rest().getProjects();                          // 获取项目列表
client.rest().getMonitoringPoints(String projectId);  // 获取监测点列表
```

### MqttSubscriber

```java
client.mqtt().connect();              // 连接 MQTT
client.mqtt().disconnect();           // 断开连接
client.mqtt().isConnected();          // 检查连接状态
client.mqtt().subscribeProject(...);  // 订阅项目数据
client.mqtt().subscribeDevice(...);   // 订阅设备数据
client.mqtt().subscribePoint(...);    // 订阅监测点数据
```

## 系统要求

- Java 11 或更高版本
- Maven 3.6+（用于构建）

## 构建

```bash
# 构建
mvn clean package

# 运行测试
mvn test

# 安装到本地仓库
mvn install
```

## 环境变量（测试用）

```bash
export INTEAGLE_API_ENDPOINT=https://api.shm.inteagle.com
export INTEAGLE_MQTT_HOST=broker.shm.inteagle.com
export INTEAGLE_MQTT_PORT=8883
export INTEAGLE_CUSTOMER_ID=your-customer-id
export INTEAGLE_ACCESS_TOKEN=your-access-token
```

## 许可证

Apache License 2.0

## 支持

- 文档: https://docs.inteagle.com
- 问题反馈: https://github.com/inteagle-vision/shm-sdk/issues
- 更新日志: [CHANGELOG.md](CHANGELOG.md)
- 迁移指南: [MIGRATION.md](MIGRATION.md)
