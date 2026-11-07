package com.skyshow.dispatch.common;

/**
 * 统一返回信封：所有接口都返回 { code, message, data }
 */
public class Result<T> {

    private int code;
    private String message;
    private T data;

    // 构造方法设为 private：外面只能走 success / error，信封格式不会乱
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功：把数据包进 200 信封
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    // 失败：只有错误码和提示，data 为空
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    // getter：字段是 private，外部读取只能通过这些方法
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}