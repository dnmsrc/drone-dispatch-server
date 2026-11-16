package com.skyshow.dispatch.dto.dict;

/**
 * 字典类型列表行（汇总形态）：类型以业务键 dictCode 标识，不返回自增 id。
 * itemCount / enabledCount 为聚合值，由 Service 统计填充。
 */
public record DictTypeSummaryVO(
        String dictCode,
        String dictName,
        String remark,
        Integer systemFlag,
        Integer sort,
        Integer itemCount,
        Integer enabledCount) {
}
