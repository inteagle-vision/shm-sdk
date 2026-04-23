# sign-cli

Rust 实现的 Inteagle Cloud 签名工具，可生成 MQTT broker 和 HTTP API 两种认证凭证。

## 构建

本地构建：

```bash
cargo build --release
./target/release/inteagle-sign --help
./target/release/inteagle-sign mqtt --help
./target/release/inteagle-sign http --help
```

跨平台分发（需要 Docker + [`cross`](https://github.com/cross-rs/cross)）：

```bash
cross build --release --target x86_64-unknown-linux-musl
cross build --release --target aarch64-unknown-linux-musl
cross build --release --target x86_64-pc-windows-gnu
```

产物复制到 `../../bin/sign/`：

```bash
cp target/x86_64-unknown-linux-musl/release/inteagle-sign   ../../bin/sign/inteagle-sign-linux-x86_64
cp target/aarch64-unknown-linux-musl/release/inteagle-sign  ../../bin/sign/inteagle-sign-linux-aarch64
cp target/x86_64-pc-windows-gnu/release/inteagle-sign.exe   ../../bin/sign/inteagle-sign-windows-x86_64.exe
```

## 与 Java SDK 的对应

| 命令 | 对应 Java 实现 |
| --- | --- |
| `inteagle-sign mqtt` | `src/main/java/com/inteagle/sdk/mqtt/MqttSubscriber.java` 的 `connect()` / `hmacSha256()` |
| `inteagle-sign http` | `src/main/java/com/inteagle/sdk/internal/transport/http/HmacAuthenticator.java` 的 `sign()` |

两边签名算法必须保持一致。修改时请同步更新。
