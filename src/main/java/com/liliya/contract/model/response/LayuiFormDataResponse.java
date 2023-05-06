package com.liliya.contract.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LayuiFormDataResponse <T>{
    // 接口状态，一般设置为 0 代表成功返回
    private Integer code;

    // 附带的提示信息
    private String msg;

    // 数据总数，用于分页管理
    private Long count;

    // 服务器响应数据，应为对象列表
    private T data;

    public static <T> LayuiFormDataResponse<Object> ok(String msg, Long count, T data) {
        return LayuiFormDataResponse.builder()
                .code(0)
                .msg(msg)
                .count(count)
                .data(data)
                .build();
    }

    public static LayuiFormDataResponse<Object> fail(String msg) {
        return LayuiFormDataResponse.builder()
                .code(400)
                .msg(msg)
                .build();
    }
}
