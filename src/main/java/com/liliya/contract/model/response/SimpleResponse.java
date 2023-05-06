package com.liliya.contract.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleResponse<T> implements Serializable {
    // 服务器响应时间
    private long timestamp;

    // 请求是否成功
    private boolean ok;

    // 附带的提示信息
    private String message;

    // 服务器响应数据
    private T payload;

    public static SimpleResponse<Object> ok() {
        return SimpleResponse.builder()
                .timestamp(System.currentTimeMillis() / 1000)
                .ok(true)
                .build();
    }

    public static SimpleResponse<Object> ok(String message) {
        return SimpleResponse.builder()
                .timestamp(System.currentTimeMillis() / 1000)
                .ok(true)
                .message(message)
                .build();
    }

    public static <T> SimpleResponse<Object> ok(String message, T payload) {
        return SimpleResponse.builder()
                .timestamp(System.currentTimeMillis() / 1000)
                .ok(true)
                .message(message)
                .payload(payload)
                .build();
    }

    public static SimpleResponse<Object> fail(String message) {
        return SimpleResponse.builder()
                .timestamp(System.currentTimeMillis() / 1000)
                .ok(false)
                .message(message)
                .build();
    }
}
