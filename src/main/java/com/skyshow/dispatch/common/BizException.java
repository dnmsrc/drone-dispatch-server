package com.skyshow.dispatch.common;

/**
 * 业务异常：遇到「业务规则不允许」的情况时抛出
 * 例如：档期已满、退款金额超过实付、订单状态不允许此操作
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}