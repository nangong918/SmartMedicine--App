package com.czy.api.domain.ao.post;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 13225
 * @date 2025/4/18 17:24
 */
@Data
public class PostInfoAo implements Serializable {
    // postId
    private Long id;
    // authorId；not null（索引）
    private Long authorId;
    // fileId 只展示1个
    private Long fileId;
    // title；not null
    private String title;
    // releaseTimestamp；not null
    private Long releaseTimestamp;

    // 点赞数；not null
    private Long likeCount = 0L;
    // 收藏数；not null
    private Long collectCount = 0L;
    // 评论数；not null
    private Long commentCount = 0L;
    // 转发数量
    private Long forwardCount = 0L;
}
