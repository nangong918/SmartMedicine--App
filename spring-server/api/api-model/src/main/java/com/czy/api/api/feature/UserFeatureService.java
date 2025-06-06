package com.czy.api.api.feature;

import com.czy.api.domain.ao.feature.UserEntityScore;
import com.czy.api.domain.ao.feature.UserHistoryFeatureAo;
import com.czy.api.domain.ao.feature.UserTempFeatureAo;

import java.util.List;
import java.util.Map;

/**
 * @author 13225
 * @date 2025/5/8 18:21
 * user feature context 设计：
 * 1. 发布的时候用户手动打分区标签 + Bert模型对文章进行分类：#日常分享 #专业医疗知识 #养生技巧 #医疗新闻 #其他
 * 2. 根据用户行为：
 *      发布行为；
 *      显性帖子行为：
 *          点赞，
 *          评论（BERT情感分类NLE：肯定态度，否定态度，中立态度），
 *          收藏；
 *          转发
 *      隐性行为：
 *          点击率，
 *          浏览时长（1.根据文章长度估算大概要读取的时间 - 用户已读取的时间 2.固定判断时长：超过30秒一定增加权重）
 *          搜索行为
 * 3. 热衰减：
 *       定时任务：每3天用户的全部权重*0.8；30天全部消失（存储在Redis自发消失）
 * 4.特征设计：
 *       1.分类（u-l）特征：
 *          用户对不同的标签的权重
 *       2.实体（u-a）特征：
 *          用户对不同的实体的特征权重
 *       3.物品（u-i）特征：
 *          用户对不同帖子的权重
 * service计算特征
 */
public interface UserFeatureService {

    // 获取user临时特征（redis中的全部记录统计为特征）
    UserTempFeatureAo getUserTempFeature(Long userId);

    // user历史特征：user画像
    UserHistoryFeatureAo getUserProfile(Long userId);

    // user画像集合 -> user画像list
    List<UserEntityScore> getUserProfileList(UserHistoryFeatureAo userHistoryFeatureAo);

    // 直接由userId转为List<UserEntityScore>
    List<UserEntityScore> getUserProfileList(Long userId);

    // 获取user在线特征
    List<Map<String, Double>> getUserOnlineFeature(Long userId);
}
