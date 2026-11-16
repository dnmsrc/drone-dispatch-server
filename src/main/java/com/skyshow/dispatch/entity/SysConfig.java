package com.skyshow.dispatch.entity;

import java.time.LocalDateTime;

/**
 * 系统配置，对应表 sys_config（key-value，config_value 为 JSON 列）。
 *
 * <p>只承载标量 / 数组等全局配置（支付时限、定制价、档期默认时段、预约窗口等），
 * 存什么类型由各业务 Service 解释；实体层统一按字符串读写，不耦合具体配置键。
 */
public class SysConfig {
    private Long id;
    /** 配置键，表内唯一，如 pay_expire_minutes / pricing.custom_text_price */
    private String configKey;
    /** 配置值（JSON 文本：数字、数组都以 JSON 存放） */
    private String configValue;
    /** 备注说明 */
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
