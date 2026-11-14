package com.skyshow.dispatch.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 预设表演场地，对应数据库表 venues。 */
public class Venue {
    private Long id;
    private String name;
    private String address;
    /** 经度，DECIMAL(10,6)，用 BigDecimal 不用 double */
    private BigDecimal lng;
    /** 纬度 */
    private BigDecimal lat;
    /** 启用状态：1 启用 / 0 停用，对应 TINYINT */
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** 软删除：0 正常 / 1 已删 */
    private Integer deleted;
    private LocalDateTime deletedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public void setLng(BigDecimal lng) {
        this.lng = lng;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
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

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

}
