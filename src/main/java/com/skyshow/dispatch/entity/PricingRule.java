package com.skyshow.dispatch.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 计价规则，对应表 pricing_rule。
 *
 * <p>一个分组 {@code groupCode} 下若干行，结构与原字典项同构（键/文案/数值/备注），
 * 但独立成表、归属计价域，不再借系统字典的壳。分组取值：
 * tier 架次档位 / duration 时长 / addon 附加服务 / pattern 预设图案 / refund 退款阶梯。
 */
public class PricingRule {
    private Long id;
    /** 规则分组：tier/duration/addon/pattern/refund */
    private String groupCode;
    /** 组内程序用 key：架次数 / 分钟数 / 英文 key / 退款小时下限 */
    private String itemKey;
    /** 显示文案 */
    private String itemLabel;
    /** 数值：价格 / 加价 / 退款比例(0-1)；不加价项为 0 */
    private BigDecimal itemValue;
    /** 附加说明：架次适用场景等 */
    private String extra;
    /** 组内排序号，升序 */
    private Integer sort;
    /** 1 启用 / 0 停用；停用后用户端只读接口不返回 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public BigDecimal getItemValue() {
        return itemValue;
    }

    public void setItemValue(BigDecimal itemValue) {
        this.itemValue = itemValue;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
