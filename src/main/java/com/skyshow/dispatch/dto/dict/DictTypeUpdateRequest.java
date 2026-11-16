package com.skyshow.dispatch.dto.dict;

/**
 * 修改字典类型请求体。类型标识 dict_code 不可改，定位由 URL 路径变量承担，故请求体不含它。
 *
 * @param dictName 类型显示名，必填非空白
 * @param remark   用途说明，选填
 * @param sort     排序号，选填；为空时保留原排序不变
 */
public record DictTypeUpdateRequest(
        String dictName,
        String remark,
        Integer sort) {
}
