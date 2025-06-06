package com.czy.api.domain.dto.http.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author 13225
 * @date 2025/4/21 17:26
 */
@Data
public class GetPostPreviewListRequest {
    @NotEmpty(message = "帖子 IDs 不能为空")
    public List<Long> postIds;
    @NotEmpty(message = "用户账号不能为空")
    public String userAccount;
}
