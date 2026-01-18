/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.query;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseQuery 单元测试
 */
class BaseQueryTest {

    @Test
    void testDeviceQueryBuilder() {
        DeviceQuery query = DeviceQuery.builder()
                .projectId("project-001")
                .name("sensor")
                .active(true)
                .page(1, 50)
                .orderByName(false)
                .build();

        assertEquals("project-001", query.getProjectId());
        assertEquals("sensor", query.getName());
        assertEquals(true, query.getActive());
        assertEquals(1, query.getPage());
        assertEquals(50, query.getPageSize());
        assertEquals("name", query.getOrderBy());
        assertEquals(false, query.getOrderDesc());
    }

    @Test
    void testDeviceQueryToMap() {
        DeviceQuery query = DeviceQuery.builder()
                .projectId("p1")
                .active(true)
                .page(0, 20)
                .build();

        Map<String, Object> map = query.toMap();

        assertEquals("p1", map.get("projectId"));
        assertEquals(true, map.get("active"));
        assertEquals(0, map.get("page"));
        assertEquals(20, map.get("pageSize"));
    }

    @Test
    void testAlarmQueryBuilder() {
        AlarmQuery query = AlarmQuery.builder()
                .deviceId("device-001")
                .severity("CRITICAL")
                .active(true)
                .acknowledged(false)
                .lastDays(7)
                .build();

        assertEquals("device-001", query.getDeviceId());
        assertEquals("CRITICAL", query.getSeverity());
        assertEquals(true, query.getActive());
        assertEquals(false, query.getAcknowledged());
        assertNotNull(query.getStartTs());
        assertNotNull(query.getEndTs());
    }

    @Test
    void testProjectQueryBuilder() {
        ProjectQuery query = ProjectQuery.builder()
                .name("桥梁")
                .type("bridge")
                .region("华东")
                .page(0, 10)
                .build();

        assertEquals("桥梁", query.getName());
        assertEquals("bridge", query.getType());
        assertEquals("华东", query.getRegion());
    }

    @Test
    void testPointQueryBuilder() {
        PointQuery query = PointQuery.ofProject("project-001")
                .deviceId("device-001")
                .active(true)
                .build();

        assertEquals("project-001", query.getProjectId());
        assertEquals("device-001", query.getDeviceId());
        assertEquals(true, query.getActive());
    }

    @Test
    void testTelemetryQueryBuilder() {
        TelemetryQuery query = TelemetryQuery.builder()
                .entityId("device-001")
                .keys("temperature", "humidity")
                .lastHours(2)
                .limit(100)
                .orderDesc(true)
                .build();

        assertEquals("device-001", query.getEntityId());
        assertEquals(2, query.getKeys().size());
        assertTrue(query.getKeys().contains("temperature"));
        assertTrue(query.getKeys().contains("humidity"));
        assertEquals(100, query.getLimit());
        assertEquals(true, query.getOrderDesc());
    }

    @Test
    void testTelemetryQueryWithAggregation() {
        TelemetryQuery query = TelemetryQuery.builder()
                .entityId("device-001")
                .keys("temperature")
                .lastDays(7)
                .avgPerHour()
                .build();

        assertEquals(TelemetryQuery.Aggregation.AVG, query.getAggregation());
        assertEquals(60 * 60 * 1000L, query.getIntervalMs());
    }

    @Test
    void testTelemetryQueryLatestFactory() {
        TelemetryQuery query = TelemetryQuery.latest("device-001", "temp");

        assertEquals("device-001", query.getEntityId());
        assertEquals(1, query.getLimit());
        assertEquals(true, query.getOrderDesc());
    }

    @Test
    void testCustomFilter() {
        DeviceQuery query = DeviceQuery.builder()
                .projectId("p1")
                .filter("customField", "value123")
                .build();

        Map<String, Object> map = query.toMap();
        assertEquals("value123", map.get("customField"));
    }
}
