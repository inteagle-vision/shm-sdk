/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk;

import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.model.MonitoringProject;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.ProjectQuery;

import java.time.Duration;

/**
 * 生产环境 API 测试
 *
 * 运行方式:
 * <pre>
 * export INTEAGLE_API_ENDPOINT=https://api.shm.inteagle.com
 * export INTEAGLE_ACCESS_KEY=ak_xxx
 * export INTEAGLE_SECRET_KEY=sk_xxx
 * mvn exec:java -Dexec.mainClass="com.inteagle.sdk.LiveApiTest"
 * </pre>
 */
public class LiveApiTest {

    public static void main(String[] args) {
        String endpoint = System.getenv().getOrDefault("INTEAGLE_API_ENDPOINT", "https://api.shm.inteagle.com");
        String accessKey = System.getenv("INTEAGLE_ACCESS_KEY");
        String secretKey = System.getenv("INTEAGLE_SECRET_KEY");

        if (accessKey == null || secretKey == null) {
            System.err.println("Error: Missing environment variables");
            System.err.println("Please set INTEAGLE_ACCESS_KEY and INTEAGLE_SECRET_KEY");
            System.exit(1);
        }

        System.out.println("=== Inteagle Cloud SDK Live Test ===");
        System.out.println("Endpoint: " + endpoint);
        System.out.println("Access Key: " + accessKey.substring(0, Math.min(8, accessKey.length())) + "...");
        System.out.println();

        try (InteagleClient client = InteagleClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .timeout(Duration.ofSeconds(30))
                .build()) {

            System.out.println("1. Testing Project API...");
            PageResult<MonitoringProject> projects = client.projects().list(
                    ProjectQuery.builder().pageSize(5).build()
            );
            System.out.println("   Projects found: " + projects.getTotal());
            for (MonitoringProject p : projects) {
                System.out.println("   - " + p.getName() + " (ID: " + p.getId() + ")");
            }

            System.out.println();
            System.out.println("=== SUCCESS ===");

        } catch (SdkException e) {
            System.err.println("SDK Error: " + e.getMessage());
            System.err.println("  Code: " + e.getStatusCode());
            System.err.println("  Error Code: " + e.getErrorCode());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
