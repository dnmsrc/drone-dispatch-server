package com.skyshow.dispatch.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 档期日配置，对应表 schedule_days（一天一行，仅存「与全局默认不同」的覆盖配置）。
 *
 * <p>容量口径：当日名额 = slots 时段数（一个时段一个名额），不再有独立 daily_limit 列。
 * 没有对应行的日期，运行时回退到全局默认可接单时段。
 */
public class ScheduleDay {
    private Long id;
    private LocalDate scheduleDate;
    /** 休整日标记：1 休整日（全部停接）/ 0 正常 */
    private Integer closed;
    /**
     * 当日时段 JSON 数组原始文本（元素形如 "19:00-20:00"）。
     * null = 用全局默认时段；"[]" = 当日不接单；有值 = 仅开放这些时段。
     */
    private String slots;
    /** 休整 / 节日说明 */
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public Integer getClosed() {
        return closed;
    }

    public void setClosed(Integer closed) {
        this.closed = closed;
    }

    public String getSlots() {
        return slots;
    }

    public void setSlots(String slots) {
        this.slots = slots;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
