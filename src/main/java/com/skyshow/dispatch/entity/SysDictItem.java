package com.skyshow.dispatch.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 字典项，对应数据库表 sys_dict_item（挂在某个字典类型下的键值项）。 */
public class SysDictItem {
    private Long id;
    /** 所属字典编码 → sys_dict.dict_code */
    private String dictCode;
    /** 程序用 key，如 100 / heart / WECHAT；同字典内唯一 */
    private String itemKey;
    /** 显示文案，如「100 架」 */
    private String itemLabel;
    /** 数值：价格 / 退款比例；纯文案项为 0（表允许 NULL，种子统一填 0） */
    private BigDecimal itemValue;
    /** 附加说明 */
    private String extra;
    /** 排序号，升序 */
    private Integer sort;
    /** 状态：1 启用 / 0 停用 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
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
