# Inteagle Sign CLI

独立编译的 AK/SK 签名工具，不依赖 Java / Python / Node。把 AK/SK 喂给它，
生成可以直接填到 **MQTTX / curl / Postman / Apifox** 的认证信息。

## 二进制

| 平台 | 文件 |
| --- | --- |
| Linux x86_64 | `inteagle-sign-linux-x86_64` |
| Linux aarch64 (ARM64) | `inteagle-sign-linux-aarch64` |
| Windows x86_64 | `inteagle-sign-windows-x86_64.exe` |

Linux 版本为 musl 静态链接，任意发行版直接运行。

## 子命令

```
inteagle-sign mqtt --ak ... --sk ...     # 生成 MQTT broker username/password
inteagle-sign http --ak ... --sk ... \   # 生成 HTTP API 签名 headers
               --method GET --path /api/v1/devices
```

通用环境变量：`INTEAGLE_ACCESS_KEY`、`INTEAGLE_SECRET_KEY`、`INTEAGLE_ENDPOINT`、
`INTEAGLE_CUSTOMER_ID`、`MQTT_BROKER`、`MQTT_PORT`。

---

## MQTT 签名

```bash
./inteagle-sign-linux-x86_64 mqtt --ak=YOUR_AK --sk=YOUR_SK --customer-id=cust_xxx
```

输出里 `Client ID / Username / Password` 三项照抄进 MQTTX 的新建连接面板。

要点：
- **Client ID 必须等于签名时使用的值**（默认 = AK），否则 broker 验签失败
- Password 内含时间戳，建议 60 秒内连上

算法：

```
password = "{timestamp}:{hex(HMAC-SHA256(SK, timestamp + ':' + clientId))}"
username = accessKey
```

---

## HTTP 签名

```bash
./inteagle-sign-linux-x86_64 http \
    --ak=YOUR_AK --sk=YOUR_SK \
    --method=GET --path=/api/v1/devices \
    --format=curl
```

`--format=curl` 会直接拼好完整的 curl 命令，复制即用。

输出 5 个 headers：

| Header | 说明 |
| --- | --- |
| `X-Access-Key`  | AK |
| `X-Timestamp`   | 签名时刻（毫秒） |
| `X-Nonce`       | 随机值，防重放 |
| `X-Signature`   | HMAC-SHA256 结果（hex） |
| `X-HTTP-Method` | 与 `method` 一致 |

算法：

```
signString = "{method}:{path}:{timestamp}:{nonce}"
signature  = hex( HMAC-SHA256(sk, signString) )
```

注意：
- **query 不参与签名**（工具会自动把 `?xxx` 剥掉），可以放心带查询参数
- **POST body 也不参与签名**，body 可自由变更
- `timestamp` 有服务端窗口限制（通常 5 分钟内）

---

## 签名算法来源

与 Java SDK 完全一致：

- MQTT：`src/main/java/com/inteagle/sdk/mqtt/MqttSubscriber.java`
- HTTP：`src/main/java/com/inteagle/sdk/internal/transport/http/HmacAuthenticator.java`

源码位于仓库 `tools/sign-cli/`，构建说明见该目录 README。
