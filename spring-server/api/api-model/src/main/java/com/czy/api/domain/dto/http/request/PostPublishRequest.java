package com.czy.api.domain.dto.http.request;

import com.czy.api.domain.dto.http.base.BaseNettyRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author 13225
 * @date 2025/4/21 11:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostPublishRequest extends BaseNettyRequest {
    // title；not null
    @NotEmpty(message = "标题不能为空")
    public String title;
    // content；not null
    @NotEmpty(message = "内容不能为空")
    public String content;
    @NotNull(message = "是否包含文件判断值不能为空")
    public Boolean isHaveFiles;
}
