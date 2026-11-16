package com.skyshow.dispatch.dto.dict;

import java.util.List;

/**
 * 字典类型详情：类型汇总字段 + 该类型下全部字典项（按 sort 升序）。
 */
public record DictTypeDetailVO(
        String dictCode,
        String dictName,
        String remark,
        Integer systemFlag,
        Integer itemCount,
        Integer enabledCount,
        List<DictItemVO> items) {
}
