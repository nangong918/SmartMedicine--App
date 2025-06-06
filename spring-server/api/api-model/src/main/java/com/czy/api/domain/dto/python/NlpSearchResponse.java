package com.czy.api.domain.dto.python;

import lombok.Data;

/**
 * @author 13225
 * @date 2025/5/8 17:03
 */
@Data
public class NlpSearchResponse {
    private Integer code;
    private String message;
    /**
     * 类型
     * @see com.czy.api.constant.search.NlpResultEnum
     */
    private Integer type;
}
