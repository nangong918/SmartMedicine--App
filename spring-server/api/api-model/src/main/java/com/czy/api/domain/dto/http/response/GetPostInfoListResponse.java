package com.czy.api.domain.dto.http.response;


import com.czy.api.domain.ao.post.PostInfoAo;
import lombok.Data;

import java.util.List;

/**
 * @author 13225
 * @date 2025/4/21 17:25
 */
@Data
public class GetPostInfoListResponse {
    public List<PostInfoAo> postInfoAos;
}
