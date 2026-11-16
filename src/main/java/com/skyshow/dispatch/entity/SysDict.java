package com.skyshow.dispatch.entity;

import java.time.LocalDateTime;

/** 字典类型，对应数据库表 sys_dict（若依风格两级字典的「类型」层）。 */
public class SysDict {
    private Long id;
    /** 字典编码，程序里引用它（如 drone_tier），表内唯一 */
    private String dictCode;
    /** 显示名，如「架次档位」 */
    private String dictName;
    /** 内置标记：1 内置字典（禁止删类型）/ 0 自定义 */
    private Integer systemFlag;
    private String remark;
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

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public Integer getSystemFlag() {
        return systemFlag;
    }

    public void setSystemFlag(Integer systemFlag) {
        this.systemFlag = systemFlag;
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
