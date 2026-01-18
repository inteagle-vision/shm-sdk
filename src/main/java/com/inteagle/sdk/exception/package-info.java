/*
 * Copyright © 2026 Inteagle Inc.
 */

/**
 * Inteagle Cloud SDK 异常体系
 *
 * <p>异常层次结构:
 * <pre>
 * {@link com.inteagle.sdk.exception.SdkException}
 * ├── {@link com.inteagle.sdk.exception.AuthenticationException}  (401)
 * ├── {@link com.inteagle.sdk.exception.NotFoundException}        (404)
 * ├── {@link com.inteagle.sdk.exception.RateLimitException}       (429)
 * ├── {@link com.inteagle.sdk.exception.ConnectionException}      (网络错误)
 * └── {@link com.inteagle.sdk.exception.ServerException}          (5xx)
 * </pre>
 *
 * <p>使用示例:
 * <pre>{@code
 * try {
 *     client.devices().get("device-id");
 * } catch (NotFoundException e) {
 *     // 资源不存在
 * } catch (AuthenticationException e) {
 *     // 认证失败
 * } catch (RateLimitException e) {
 *     // 限流，等待后重试
 *     Thread.sleep(e.getRetryAfterMs());
 * } catch (SdkException e) {
 *     // 其他错误
 * }
 * }</pre>
 */
package com.inteagle.sdk.exception;
