/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.api;

import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.model.AlarmRule;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.AlarmRuleQuery;

import java.util.List;

/**
 * 告警规则 API 接口
 * <p>
 * 提供告警规则的查询功能。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 分页查询告警规则
 * PageResult<AlarmRule> rules = client.alarmRules().list(
 *     AlarmRuleQuery.builder()
 *         .projectId("project-001")
 *         .pageSize(20)
 *         .build()
 * );
 *
 * // 查询某设备的告警规则
 * List<AlarmRule> rules = client.alarmRules().listAll(
 *     AlarmRuleQuery.ofEntity("project-001", "DEVICE", "device-001")
 * );
 *
 * // 获取单个告警规则
 * AlarmRule rule = client.alarmRules().get("project-001", "rule-001");
 * }</pre>
 */
public interface AlarmRuleApi {

    /**
     * 分页查询告警规则
     *
     * @param query 查询条件
     * @return 分页结果
     * @throws SdkException 如果查询失败
     */
    PageResult<AlarmRule> list(AlarmRuleQuery query) throws SdkException;

    /**
     * 查询所有告警规则 (不分页)
     *
     * @param query 查询条件
     * @return 告警规则列表
     * @throws SdkException 如果查询失败
     */
    List<AlarmRule> listAll(AlarmRuleQuery query) throws SdkException;

    /**
     * 获取单个告警规则
     *
     * @param projectId 项目 ID
     * @param ruleId    规则 ID
     * @return 告警规则
     * @throws com.inteagle.sdk.exception.NotFoundException 如果规则不存在
     * @throws SdkException                                  如果查询失败
     */
    AlarmRule get(String projectId, String ruleId) throws SdkException;
}
