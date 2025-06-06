package com.czy.api.domain.Do.post.post;

import json.BaseBean;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * @author 13225
 * @date 2025/4/16 21:24
 * 朋友圈的infoDo
 * 存放在MySQL
 */
@Data
public class PostInfoDo implements BaseBean , Serializable {
    // id；postDetails的id与postInfo的id一致
    @Id
    private Long id;
    // authorId；not null（索引）
    private Long authorId;
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
    // 不感兴趣数量
    private Long notInterestedCount = 0L;
}
