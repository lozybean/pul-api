package me.lyon.pul.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class WebResponse<T> {
    private T data;
    private String status;
    private String msg;

    public static <T> WebResponse<T> ok(T data) {
        return WebResponse.<T>builder()
                .data(data)
                .status("OK")
                .build();
    }

    public static <T> WebResponse<T> warn(String message) {
        return WebResponse.<T>builder()
                .status("WARNING")
                .msg(message)
                .build();
    }
}
