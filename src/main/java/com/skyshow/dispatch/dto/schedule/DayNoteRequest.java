package com.skyshow.dispatch.dto.schedule;

/**
 * 单日说明请求体（PUT /api/schedule/days/{date}/note）。
 *
 * <p>只写说明（节假日 / 特殊日标注），不改休整状态与接单时段；空串表示取消标注。
 *
 * @param note 说明文字，最多 100 字（DB 为 VARCHAR(100)）
 */
public record DayNoteRequest(String note) {
}