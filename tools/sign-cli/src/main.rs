use clap::{Args, Parser, Subcommand, ValueEnum};
use hmac::{Hmac, Mac};
use sha2::Sha256;
use std::time::{SystemTime, UNIX_EPOCH};

type HmacSha256 = Hmac<Sha256>;

#[derive(Copy, Clone, Debug, PartialEq, Eq, ValueEnum)]
enum MqttFormat {
    Pretty,
    Env,
    Json,
}

#[derive(Copy, Clone, Debug, PartialEq, Eq, ValueEnum)]
enum HttpFormat {
    Pretty,
    Curl,
    Env,
    Json,
}

/// Inteagle Cloud 签名工具 (HMAC-SHA256)。生成 MQTT 或 HTTP 认证所需的凭证，
/// 可直接粘到 MQTTX / curl / Postman / Apifox 里使用。
#[derive(Parser, Debug)]
#[command(name = "inteagle-sign", version, about, long_about = None)]
struct Cli {
    #[command(subcommand)]
    cmd: Cmd,
}

#[derive(Subcommand, Debug)]
enum Cmd {
    /// 生成 MQTT broker 认证的 username / password
    Mqtt(MqttArgs),
    /// 生成 HTTP API 签名 headers
    Http(HttpArgs),
}

#[derive(Args, Debug)]
struct MqttArgs {
    /// Access Key (同时用作 MQTT Username 和 Client ID 默认值)
    #[arg(long, env = "INTEAGLE_ACCESS_KEY")]
    ak: String,

    /// Secret Key (仅本地 HMAC，不发送到 broker)
    #[arg(long, env = "INTEAGLE_SECRET_KEY")]
    sk: String,

    /// MQTT broker 主机
    #[arg(long, env = "MQTT_BROKER", default_value = "broker.shm.inteagle.com")]
    host: String,

    /// MQTT broker 端口（未指定时：TLS=8883, 否则=21883）
    #[arg(long, env = "MQTT_PORT")]
    port: Option<u16>,

    /// 使用 TLS (MQTTX 需同时开启 SSL/TLS)
    #[arg(long)]
    tls: bool,

    /// 自定义 Client ID (默认等于 --ak)
    #[arg(long)]
    client_id: Option<String>,

    /// 自定义签名时间戳（毫秒），默认当前时间
    #[arg(long)]
    timestamp: Option<i64>,

    /// Customer ID（如提供，输出里会附上 topic 前缀）
    #[arg(long, env = "INTEAGLE_CUSTOMER_ID")]
    customer_id: Option<String>,

    /// 输出格式
    #[arg(long, value_enum, default_value_t = MqttFormat::Pretty)]
    format: MqttFormat,
}

#[derive(Args, Debug)]
struct HttpArgs {
    /// Access Key
    #[arg(long, env = "INTEAGLE_ACCESS_KEY")]
    ak: String,

    /// Secret Key (仅本地 HMAC，不发送)
    #[arg(long, env = "INTEAGLE_SECRET_KEY")]
    sk: String,

    /// HTTP 方法 (GET / POST / PUT / DELETE)
    #[arg(short = 'm', long)]
    method: String,

    /// 请求路径，可以带 query 字符串（签名时 query 会被自动剥离）
    #[arg(short = 'p', long)]
    path: String,

    /// API endpoint（用于生成 curl 示例，不参与签名）
    #[arg(long, env = "INTEAGLE_ENDPOINT", default_value = "https://api.shm.inteagle.com")]
    endpoint: String,

    /// 自定义签名时间戳（毫秒），默认当前时间
    #[arg(long)]
    timestamp: Option<i64>,

    /// 自定义 nonce，默认随机 UUID (去横杠)
    #[arg(long)]
    nonce: Option<String>,

    /// 输出格式
    #[arg(long, value_enum, default_value_t = HttpFormat::Pretty)]
    format: HttpFormat,
}

fn hmac_sha256_hex(secret: &str, data: &str) -> String {
    let mut mac = HmacSha256::new_from_slice(secret.as_bytes())
        .expect("HMAC can accept keys of any size");
    mac.update(data.as_bytes());
    hex::encode(mac.finalize().into_bytes())
}

fn now_millis() -> i64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_millis() as i64)
        .unwrap_or(0)
}

fn main() {
    let cli = Cli::parse();
    match cli.cmd {
        Cmd::Mqtt(args) => run_mqtt(args),
        Cmd::Http(args) => run_http(args),
    }
}

// ==================== MQTT ====================

fn run_mqtt(args: MqttArgs) {
    let client_id = args.client_id.unwrap_or_else(|| args.ak.clone());
    let timestamp = args.timestamp.unwrap_or_else(now_millis);
    let port = args.port.unwrap_or(if args.tls { 8883 } else { 21883 });
    let scheme = if args.tls { "ssl" } else { "tcp" };
    let broker_url = format!("{}://{}:{}", scheme, args.host, port);

    let sign_string = format!("{}:{}", timestamp, client_id);
    let signature = hmac_sha256_hex(&args.sk, &sign_string);
    let password = format!("{}:{}", timestamp, signature);

    match args.format {
        MqttFormat::Pretty => mqtt_pretty(
            &broker_url, &args.host, port, args.tls, &client_id,
            &args.ak, &password, timestamp, args.customer_id.as_deref(),
        ),
        MqttFormat::Env => mqtt_env(
            &broker_url, &args.host, port, &client_id,
            &args.ak, &password, args.customer_id.as_deref(),
        ),
        MqttFormat::Json => mqtt_json(
            &broker_url, &args.host, port, args.tls, &client_id,
            &args.ak, &password, timestamp, args.customer_id.as_deref(),
        ),
    }
}

fn mqtt_pretty(
    broker_url: &str, host: &str, port: u16, tls: bool, client_id: &str,
    username: &str, password: &str, timestamp: i64, customer_id: Option<&str>,
) {
    let bar = "━".repeat(60);
    println!("{bar}");
    println!("  MQTT 签名凭证 (可直接填入 MQTTX / mosquitto_sub)");
    println!("{bar}");
    println!("  Broker URL : {broker_url}");
    println!("  Host       : {host}");
    println!("  Port       : {port}");
    println!("  TLS        : {}", if tls { "是 (MQTTX 需开启 SSL/TLS)" } else { "否" });
    println!();
    println!("  Client ID  : {client_id}");
    println!("  Username   : {username}");
    println!("  Password   : {password}");
    println!();
    println!("  签名时间戳 : {timestamp}");
    println!("  签名算法   : hex(HMAC-SHA256(SK, \"{{timestamp}}:{{clientId}}\"))");
    if let Some(cid) = customer_id {
        println!();
        println!("  Customer ID: {cid}");
        println!("  订阅所有   : inteagle/{cid}/#");
        println!("  订阅设备   : inteagle/{cid}/d/{{deviceId}}");
        println!("  订阅项目   : inteagle/{cid}/p/{{projectId}}/#");
    }
    println!();
    println!("  ⚠ Client ID 必须与此处一致，否则 broker 签名校验失败");
    println!("  ⚠ Password 内含时间戳，建议 60 秒内完成连接");
}

fn mqtt_env(
    broker_url: &str, host: &str, port: u16, client_id: &str,
    username: &str, password: &str, customer_id: Option<&str>,
) {
    println!("MQTT_BROKER_URL={broker_url}");
    println!("MQTT_HOST={host}");
    println!("MQTT_PORT={port}");
    println!("MQTT_CLIENT_ID={client_id}");
    println!("MQTT_USERNAME={username}");
    println!("MQTT_PASSWORD={password}");
    if let Some(cid) = customer_id {
        println!("MQTT_CUSTOMER_ID={cid}");
        println!("MQTT_TOPIC_PREFIX=inteagle/{cid}");
    }
}

fn mqtt_json(
    broker_url: &str, host: &str, port: u16, tls: bool, client_id: &str,
    username: &str, password: &str, timestamp: i64, customer_id: Option<&str>,
) {
    let mut obj = serde_json::Map::new();
    obj.insert("brokerUrl".into(), broker_url.into());
    obj.insert("host".into(), host.into());
    obj.insert("port".into(), port.into());
    obj.insert("tls".into(), tls.into());
    obj.insert("clientId".into(), client_id.into());
    obj.insert("username".into(), username.into());
    obj.insert("password".into(), password.into());
    obj.insert("timestamp".into(), timestamp.into());
    if let Some(cid) = customer_id {
        obj.insert("customerId".into(), cid.into());
        obj.insert("topicPrefix".into(), format!("inteagle/{cid}").into());
    }
    println!("{}", serde_json::to_string_pretty(&serde_json::Value::Object(obj)).unwrap());
}

// ==================== HTTP ====================

fn run_http(args: HttpArgs) {
    let method = args.method.to_uppercase();
    let sign_path = match args.path.find('?') {
        Some(i) => &args.path[..i],
        None => &args.path,
    };
    let timestamp = args.timestamp.unwrap_or_else(now_millis);
    let nonce = args
        .nonce
        .filter(|s| !s.is_empty())
        .unwrap_or_else(|| uuid::Uuid::new_v4().simple().to_string());

    let sign_string = format!("{}:{}:{}:{}", method, sign_path, timestamp, nonce);
    let signature = hmac_sha256_hex(&args.sk, &sign_string);

    let endpoint = args.endpoint.trim_end_matches('/');
    let full_url = format!("{}{}", endpoint, args.path);

    match args.format {
        HttpFormat::Pretty => http_pretty(
            &full_url, &method, sign_path, &args.path, &args.ak,
            timestamp, &nonce, &signature, &sign_string,
        ),
        HttpFormat::Curl => http_curl(&full_url, &method, &args.ak, timestamp, &nonce, &signature),
        HttpFormat::Env => http_env(&args.ak, timestamp, &nonce, &signature, &method),
        HttpFormat::Json => http_json(
            &full_url, &method, sign_path, &args.path, &args.ak,
            timestamp, &nonce, &signature,
        ),
    }
}

fn http_pretty(
    full_url: &str, method: &str, sign_path: &str, raw_path: &str, ak: &str,
    timestamp: i64, nonce: &str, signature: &str, sign_string: &str,
) {
    let bar = "━".repeat(60);
    println!("{bar}");
    println!("  HTTP 签名 headers (可直接粘到 curl / Postman / Apifox)");
    println!("{bar}");
    println!("  Method        : {method}");
    println!("  Request Path  : {raw_path}");
    if raw_path != sign_path {
        println!("  Signed Path   : {sign_path}   (query 不参与签名)");
    }
    println!("  Full URL      : {full_url}");
    println!();
    println!("  X-Access-Key  : {ak}");
    println!("  X-Timestamp   : {timestamp}");
    println!("  X-Nonce       : {nonce}");
    println!("  X-Signature   : {signature}");
    println!("  X-HTTP-Method : {method}");
    println!();
    println!("  签名材料      : {sign_string}");
    println!("  签名算法      : hex(HMAC-SHA256(SK, \"method:path:timestamp:nonce\"))");
    println!();
    println!("  ⚠ POST 请求 body 不参与签名，仍可随意变更");
    println!("  ⚠ 时间戳建议在服务端允许窗口内使用（通常 5 分钟）");
}

fn http_curl(
    full_url: &str, method: &str, ak: &str,
    timestamp: i64, nonce: &str, signature: &str,
) {
    println!(
        "curl -X {method} \\\n  \
         -H 'X-Access-Key: {ak}' \\\n  \
         -H 'X-Timestamp: {timestamp}' \\\n  \
         -H 'X-Nonce: {nonce}' \\\n  \
         -H 'X-Signature: {signature}' \\\n  \
         -H 'X-HTTP-Method: {method}' \\\n  \
         -H 'Content-Type: application/json' \\\n  \
         '{full_url}'"
    );
}

fn http_env(ak: &str, timestamp: i64, nonce: &str, signature: &str, method: &str) {
    println!("X_ACCESS_KEY={ak}");
    println!("X_TIMESTAMP={timestamp}");
    println!("X_NONCE={nonce}");
    println!("X_SIGNATURE={signature}");
    println!("X_HTTP_METHOD={method}");
}

fn http_json(
    full_url: &str, method: &str, sign_path: &str, raw_path: &str, ak: &str,
    timestamp: i64, nonce: &str, signature: &str,
) {
    let mut headers = serde_json::Map::new();
    headers.insert("X-Access-Key".into(), ak.into());
    headers.insert("X-Timestamp".into(), timestamp.to_string().into());
    headers.insert("X-Nonce".into(), nonce.into());
    headers.insert("X-Signature".into(), signature.into());
    headers.insert("X-HTTP-Method".into(), method.into());

    let mut obj = serde_json::Map::new();
    obj.insert("method".into(), method.into());
    obj.insert("path".into(), raw_path.into());
    obj.insert("signedPath".into(), sign_path.into());
    obj.insert("url".into(), full_url.into());
    obj.insert("headers".into(), serde_json::Value::Object(headers));
    obj.insert("timestamp".into(), timestamp.into());
    obj.insert("nonce".into(), nonce.into());
    obj.insert("signature".into(), signature.into());
    println!("{}", serde_json::to_string_pretty(&serde_json::Value::Object(obj)).unwrap());
}