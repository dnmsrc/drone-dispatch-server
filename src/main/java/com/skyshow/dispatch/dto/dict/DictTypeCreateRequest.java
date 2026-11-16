package com.skyshow.dispatch.dto.dict;

/**
 * 新增字典类型请求体。
 * 自定义类型 system_flag 由服务端恒置 0，不接收前端传入；id、时间戳为系统字段。
 *
 * @param dictCode 类型标识，正则 ^[a-z][a-z0-9_]{1,31}$ 且库内唯一
 * @param dictName 类型显示名，必填非空白
 * @param remark   用途说明，选填
 */
public record DictTypeCreateRequest(
        String dictCode,
        String dictName,
        String remark) {
}
