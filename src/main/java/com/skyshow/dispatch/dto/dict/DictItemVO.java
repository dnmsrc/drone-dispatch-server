package com.skyshow.dispatch.dto.dict;

import java.math.BigDecimal;

/**
 * 字典项出参：只暴露 7 个业务字段，不含 created_at/updated_at/dict_code 等内部列。
 * 字段名保持数据库语义驼峰，前端适配层负责改名为 key/label/value。
 */
public record DictItemVO(
        Long id,
        String itemKey,
        String itemLabel,
        BigDecimal itemValue,
        String extra,
        Integer sort,
        Integer status) {
}
