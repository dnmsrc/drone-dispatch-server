package com.skyshow.dispatch.dto.dict;

import java.math.BigDecimal;

/**
 * 新增 / 修改字典项共用请求体（两个 record 字段一致，同场地写接口风格）。
 * 所属类型由 URL 路径 dictCode 承担、主键 id 走路径，均不放进请求体。
 *
 * @param itemKey   程序用键，同类型内唯一，必填
 * @param itemLabel 显示文案，必填非空白
 * @param itemValue 数值（价格/比例），可空，服务端缺省归 0
 * @param extra     附加说明，选填
 * @param sort      排序号，可空，新增缺省为当前项数 + 1
 * @param status    1 启用 / 0 停用，可空，缺省 1
 */
public record DictItemSaveRequest(
        String itemKey,
        String itemLabel,
        BigDecimal itemValue,
        String extra,
        Integer sort,
        Integer status) {
}
