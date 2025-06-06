package com.czy.feature.service;

import com.czy.api.api.feature.PostFeatureService;
import com.czy.api.api.feature.UserFeatureService;
import com.czy.api.constant.feature.FeatureConstant;
import com.czy.api.constant.feature.FeatureTypeChanger;
import com.czy.api.constant.feature.PostTypeEnum;
import com.czy.api.constant.feature.UserActionRedisKey;
import com.czy.api.constant.post.DiseasesKnowledgeGraphEnum;
import com.czy.api.domain.Do.neo4j.rels.UserEntityRelation;
import com.czy.api.domain.ao.feature.*;
import com.czy.api.domain.ao.post.PostNerResult;
import com.czy.api.mapper.UserFeatureRepository;
import com.czy.feature.rule.RuleHistoryFeature;
import com.czy.feature.rule.RuleTempFeature;
import com.utils.mvc.redisson.RedissonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 13225
 * @date 2025/5/13 11:50
 * 关于特征的处理，clickTime，implicitScore，explicitScore需要配置
 * 将全部超参数提取出来方便配置
 */
@Slf4j
@RequiredArgsConstructor
@Service
@org.apache.dubbo.config.annotation.Service(protocol = "dubbo", version = "1.0.0")
public class UserFeatureServiceImpl implements UserFeatureService {

    private final RedissonService redissonService;
    private final PostFeatureService postFeatureService;
    private final UserFeatureRepository userFeatureRepository;
    
    private final RuleTempFeature ruleTempFeature;
    private final RuleHistoryFeature ruleHistoryFeature;
    
    @Override
    public UserTempFeatureAo getUserTempFeature(Long userId) {
        // 1.计算时间戳
        long currentTime = System.currentTimeMillis();
        // 30天前的时间戳
        long thirtyDaysAgoTime = currentTime - FeatureConstant.FEATURE_EXPIRE_TIME_SECOND * 1000L;

        // 2.获取Redis临时特征
        String userClickFeatureKey = UserActionRedisKey.USER_FEATURE_CLICK_POST_REDIS_KEY + userId + ":*";
        String userBrowseFeatureKey = UserActionRedisKey.USER_FEATURE_BROWSE_POST_REDIS_KEY + userId + ":*";
        String userSearchFeatureKey = UserActionRedisKey.USER_FEATURE_SEARCH_POST_REDIS_KEY + userId;
        String userOperateFeatureKey = UserActionRedisKey.USER_FEATURE_OPERATION_POST_REDIS_KEY + userId;
        String userCommentFeatureKey = UserActionRedisKey.USER_FEATURE_COMMENT_POST_REDIS_KEY + userId;

        // PostClickTimeAo(postId)
        Collection<String> userClickFeatureKeys = redissonService.getKeysByPattern(userClickFeatureKey);
        Collection<Object> userClickFeatures = new ArrayList<>();
        for (String key : userClickFeatureKeys){
            Collection<Object> userClickFeature = redissonService.zRangeByScore(
                    key,
                    (double) thirtyDaysAgoTime,
                    (double) currentTime);
            userClickFeatures.addAll(userClickFeature);
        }

        // PostBrowseDurationAo(implicitScore)
        Collection<String> userBrowseFeatureKeys = redissonService.getKeysByPattern(userBrowseFeatureKey);
        Collection<Object> userBrowseFeatures = new ArrayList<>();
        for (String key : userBrowseFeatureKeys){
            Collection<Object> userBrowseFeature = redissonService.zRangeByScore(
                    key,
                    (double) thirtyDaysAgoTime,
                    (double) currentTime);
            userBrowseFeatures.addAll(userBrowseFeature);
        }

        /*
        PostExplicitTimeAo(
            List<PostExplicitPostScoreAo>,
//            List<PostExplicitEntityScoreAo>,
//            List<PostExplicitLabelScoreAo>
            )
         */
        Collection<Object> userSearchFeature = redissonService.zRangeByScore(
                userSearchFeatureKey,
                (double) thirtyDaysAgoTime,
                (double) currentTime);
        // PostExplicitTimeAo
        Collection<Object> userOperateFeature = redissonService.zRangeByScore(
                userOperateFeatureKey,
                (double) thirtyDaysAgoTime,
                (double) currentTime);
        // PostExplicitTimeAo
        Collection<Object> userCommentFeature = redissonService.zRangeByScore(
                userCommentFeatureKey,
                (double) thirtyDaysAgoTime,
                (double) currentTime
        );

        List<Collection<Object>> userFeatureList = new ArrayList<>();
        userFeatureList.add(userClickFeatures);
        userFeatureList.add(userBrowseFeatures);
        userFeatureList.add(userSearchFeature);
        userFeatureList.add(userOperateFeature);
        userFeatureList.add(userCommentFeature);

        // 3.处理特征
        return getUserTempFeatureAo(
                userFeatureList
        );
    }




    // 处理实体和标签特征的通用方法
    private void processEntityAndLabelFeatures(Long postId, Long timestamp, Double score,
                                               Map<String, NerFeatureScoreDaysAo> nerFeatureScoreMap,
                                               Map<Integer, List<ScoreDaysAo>> labelScoreMap) {
        PostFeatureAo postFeatureAo = postFeatureService.getPostFeature(postId);
        if (postFeatureAo == null) {
            return;
        }

        // 处理实体特征
        if (!CollectionUtils.isEmpty(postFeatureAo.getPostNerResultList())) {
            for (PostNerResult postNerResult : postFeatureAo.getPostNerResultList()) {
                String keyWord = postNerResult.getKeyWord();
                NerFeatureScoreDaysAo nerFeature = nerFeatureScoreMap.computeIfAbsent(
                        keyWord, k -> new NerFeatureScoreDaysAo());

                ScoreDaysAo entityScoreDaysAo = new ScoreDaysAo();
                entityScoreDaysAo.setDays(getDays(timestamp));
                entityScoreDaysAo.getScoreAo().setImplicitScore(score);
                nerFeature.getScoreDaysAoList().add(entityScoreDaysAo);
                nerFeature.setNerType(postNerResult.getNerType());
                nerFeature.setKeyWord(keyWord);
            }
        }

        // 处理标签特征
        Integer label = postFeatureAo.getPostType();
        PostTypeEnum postTypeEnum = PostTypeEnum.getByCode(label);
        if (postTypeEnum == PostTypeEnum.OTHER) {
            return;
        }

        List<ScoreDaysAo> labelScores = labelScoreMap.computeIfAbsent(label, k -> new ArrayList<>());
        ScoreDaysAo labelScoreDaysAo = new ScoreDaysAo();
        labelScoreDaysAo.setDays(getDays(timestamp));
        labelScoreDaysAo.getScoreAo().setImplicitScore(score);
        labelScores.add(labelScoreDaysAo);
    }

    // 定义特征处理器接口
    interface FeatureProcessor<T> {
        void process(T feature,
                     Map<Long, List<ScoreDaysAo>> postFeaturesMap,
                     Map<String, NerFeatureScoreDaysAo> nerFeatureScoreMap,
                     Map<Integer, List<ScoreDaysAo>> labelScoreMap);
    }

    // 通用特征处理方法
    private <T> void processFeatureCollection(Collection<Object> features, Class<T> clazz,
                                              FeatureProcessor<T> processor,
                                              Map<Long, List<ScoreDaysAo>> postFeaturesMap,
                                              Map<String, NerFeatureScoreDaysAo> nerFeatureScoreMap,
                                              Map<Integer, List<ScoreDaysAo>> labelScoreMap) {
        if (CollectionUtils.isEmpty(features)){
            return;
        }

        features.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .forEach(f -> processor.process(f, postFeaturesMap, nerFeatureScoreMap,
                        labelScoreMap));
    }

    private void processPostFeatures(PostExplicitTimeAo feature,
                                     Map<Long, List<ScoreDaysAo>> postFeaturesMap,
                                     Map<String, NerFeatureScoreDaysAo> nerFeatureScoreMap,
                                     Map<Integer, List<ScoreDaysAo>> labelScoreMap) {
        for (PostExplicitPostScoreAo scoreAo : feature.getPostExplicitPostScoreAos()) {
            Long postId = scoreAo.getPostId();
            Long timestamp = scoreAo.getTimestamp();
            if (timestamp == null) {
                continue;
            }

            // 处理帖子post特征
            List<ScoreDaysAo> postFeatures = postFeaturesMap.computeIfAbsent(postId, k -> new ArrayList<>());
            ScoreDaysAo postScoreDaysAo = new ScoreDaysAo();
            postScoreDaysAo.setDays(getDays(timestamp));
            postScoreDaysAo.getScoreAo().setImplicitScore(scoreAo.getScore());
            postFeatures.add(postScoreDaysAo);

            // 处理实体和标签entity-label特征
            processEntityAndLabelFeatures(postId, timestamp, scoreAo.getScore(), nerFeatureScoreMap, labelScoreMap);
        }
    }

    private UserTempFeatureAo getUserTempFeatureAo(List<Collection<Object>> userFeatureList){
        Map<Long, List<ScoreDaysAo>> postFeaturesMap = new HashMap<>();
        Map<String, NerFeatureScoreDaysAo> nerFeatureScoreMap = new HashMap<>();
        Map<Integer, List<ScoreDaysAo>> labelScoreMap = new HashMap<>();

        // 1.点击特征处理器
        FeatureProcessor<PostClickTimeAo> clickProcessor = (
                feature, postMap, nerMap,
                labelMap) -> {
            Long postId = feature.getPostId();
            Long clickTime = feature.getClickTime();
            if (clickTime == null){
                return;
            }

            List<ScoreDaysAo> postFeatures = postMap.computeIfAbsent(postId, k -> new ArrayList<>());
            ScoreDaysAo scoreDaysAo = new ScoreDaysAo();
            scoreDaysAo.setDays(getDays(clickTime));
            int clickTimes = Math.toIntExact(feature.getClickTime());
            scoreDaysAo.getScoreAo().setClickTimes(clickTimes);
            postFeatures.add(scoreDaysAo);
        };

        // 2.浏览特征处理器
        FeatureProcessor<PostBrowseDurationAo> browseProcessor = (
                feature, postMap, nerMap,
                labelMap) -> {
            Long postId = feature.getPostId();
            Long timestamp = feature.getTimestamp();
            if (timestamp == null) {
                return;
            }

            // 处理帖子特征
            List<ScoreDaysAo> postFeatures = postMap.computeIfAbsent(postId, k -> new ArrayList<>());
            ScoreDaysAo postScoreDaysAo = new ScoreDaysAo();
            postScoreDaysAo.setDays(getDays(timestamp));
            postScoreDaysAo.getScoreAo().setImplicitScore(feature.getImplicitScore());
            postFeatures.add(postScoreDaysAo);

            // 处理实体和标签特征
            processEntityAndLabelFeatures(postId, timestamp, feature.getImplicitScore(),
                    nerMap, labelMap);
        };

        // 3.搜索特征处理器
        FeatureProcessor<PostExplicitTimeAo> searchProcessor =
                this::processPostFeatures;

        // 4.操作特征处理器
        FeatureProcessor<PostExplicitTimeAo> operateProcessor =
                this::processPostFeatures;

        // 5.评论帖子处理器
        FeatureProcessor<PostExplicitTimeAo> commentProcessor =
                this::processPostFeatures;

        // 6.处理各类型特征
        processFeatureCollection(userFeatureList.get(0), PostClickTimeAo.class, clickProcessor,
                postFeaturesMap, nerFeatureScoreMap, labelScoreMap);
        processFeatureCollection(userFeatureList.get(1), PostBrowseDurationAo.class, browseProcessor,
                postFeaturesMap, nerFeatureScoreMap, labelScoreMap);
        processFeatureCollection(userFeatureList.get(2), PostExplicitTimeAo.class, searchProcessor,
                postFeaturesMap, nerFeatureScoreMap, labelScoreMap);
        processFeatureCollection(userFeatureList.get(3), PostExplicitTimeAo.class, operateProcessor,
                postFeaturesMap, nerFeatureScoreMap, labelScoreMap);
        processFeatureCollection(userFeatureList.get(4), PostExplicitTimeAo.class, commentProcessor,
                postFeaturesMap, nerFeatureScoreMap, labelScoreMap);

        // 叠加计算
        Map<Long, ScoreAo> postAllFeaturesMap = new HashMap<>();
        Map<String, NerFeatureScoreAo> nerAllFeatureScoreMap = new HashMap<>();
        Map<Integer, ScoreAo> labelAllScoreMap = new HashMap<>();

        for (Map.Entry<Long, List<ScoreDaysAo>> entry : postFeaturesMap.entrySet()) {
            Long postId = entry.getKey();
            List<ScoreDaysAo> scoreDaysAos = entry.getValue();
            ScoreAo postScore = ruleTempFeature.execute(scoreDaysAos);
            postAllFeaturesMap.put(postId, postScore);
        }
        for (Map.Entry<String, NerFeatureScoreDaysAo> entry : nerFeatureScoreMap.entrySet()) {
            String keyWord = entry.getKey();
            NerFeatureScoreDaysAo nerFeatureScoreDaysAo = entry.getValue();
            if (!nerFeatureScoreDaysAo.isEmpty()) {
                List<ScoreDaysAo> scoreDaysAos = nerFeatureScoreDaysAo.getScoreDaysAoList();
                ScoreAo nerScore = ruleTempFeature.execute(scoreDaysAos);
                NerFeatureScoreAo nerFeatureScoreAo = new NerFeatureScoreAo();
                nerFeatureScoreAo.setKeyWord(keyWord);
                nerFeatureScoreAo.setScore(nerScore);
                nerFeatureScoreAo.setNerType(nerFeatureScoreDaysAo.getNerType());
                nerAllFeatureScoreMap.put(keyWord, nerFeatureScoreAo);
            }
        }
        for (Map.Entry<Integer, List<ScoreDaysAo>> entry : labelScoreMap.entrySet()) {
            Integer label = entry.getKey();
            List<ScoreDaysAo> scoreDaysAos = entry.getValue();
            ScoreAo labelScore = ruleTempFeature.execute(scoreDaysAos);
            labelAllScoreMap.put(label, labelScore);
        }

        UserTempFeatureAo userTempFeatureAo = new UserTempFeatureAo();
        userTempFeatureAo.setPostScoreMap(postAllFeaturesMap);
        userTempFeatureAo.setNerFeatureScoreMap(nerAllFeatureScoreMap);
        userTempFeatureAo.setLabelScoreMap(labelAllScoreMap);
        return userTempFeatureAo;
    }

/*    private UserTempFeatureAo getUserTempFeatureAo(
            List<Collection<Object>> userFeatureList
    ){
        Collection<Object> userClickFeature = userFeatureList.get(0);
        Collection<Object> userBrowseFeature = userFeatureList.get(1);
        Collection<Object> userSearchFeature = userFeatureList.get(2);
        Collection<Object> userOperateFeature = userFeatureList.get(3);
        Collection<Object> userCommentFeature = userFeatureList.get(4);

        Map<Long, List<ScoreDaysAo>> postFeaturesMap = new HashMap<>();
        Map<String, NerFeatureScoreDaysAo> nerFeatureScoreMap = new HashMap<>();
        Map<Integer, List<ScoreDaysAo>> labelScoreMap = new HashMap<>();

        // 处理 点击特征
        if (!CollectionUtils.isEmpty(userClickFeature)){
            for (Object o : userClickFeature) {
                if (o instanceof PostClickTimeAo){
                    Long postId = ((PostClickTimeAo) o).getPostId();
                    List<ScoreDaysAo> postFeatures = postFeaturesMap.computeIfAbsent(
                            postId,
                            k -> new ArrayList<>()
                    );
                    ScoreDaysAo scoreDaysAo = new ScoreDaysAo();
                    Long clickTime = ((PostClickTimeAo) o).getClickTime();
                    if (clickTime == null){
                        continue;
                    }
                    scoreDaysAo.setDays(getDays(clickTime));
                    int clickTimes = scoreDaysAo.getScoreAo().getClickTimes();
                    scoreDaysAo.getScoreAo().setClickTimes(clickTimes);
                    postFeatures.add(scoreDaysAo);
                    postFeaturesMap.put(postId, postFeatures); // 是指针，已经存放在内存了，此行是保险
                }
            }
        }
        // 处理 浏览特征
        if (!CollectionUtils.isEmpty(userBrowseFeature)){
            for (Object o : userBrowseFeature) {
                if (o instanceof PostBrowseDurationAo){
                    /// post
                    Long postId = ((PostBrowseDurationAo) o).getPostId();
                    List<ScoreDaysAo> postFeatures = postFeaturesMap.computeIfAbsent(
                            postId,
                            k -> new ArrayList<>()
                    );
                    ScoreDaysAo postScoreDaysAo = new ScoreDaysAo();
                    Long timestamp = ((PostBrowseDurationAo) o).getTimestamp();
                    if (timestamp == null){
                        continue;
                    }
                    postScoreDaysAo.setDays(getDays(timestamp));
                    Double implicitScore = ((PostBrowseDurationAo) o).getImplicitScore();
                    postScoreDaysAo.getScoreAo().setImplicitScore(implicitScore);
                    postFeatures.add(postScoreDaysAo);
                    postFeaturesMap.put(postId, postFeatures); // 是指针，已经存放在内存了，此行是保险

                    /// entity + label
                    PostFeatureAo postFeatureAo = postFeatureService.getPostFeature(postId);
                    if (postFeatureAo == null){
                        continue;
                    }
                    /// entity
                    if (!CollectionUtils.isEmpty(postFeatureAo.getPostNerResultList())){
                        for (PostNerResult postNerResult : postFeatureAo.getPostNerResultList()){
                            String keyWord = postNerResult.getKeyWord();
                            NerFeatureScoreDaysAo nerFeatureScoreDaysAo = nerFeatureScoreMap.computeIfAbsent(
                                    keyWord,
                                    k -> new NerFeatureScoreDaysAo()
                            );
                            ScoreDaysAo entityScoreDaysAo = new ScoreDaysAo();
                            entityScoreDaysAo.setDays(getDays(timestamp));
                            entityScoreDaysAo.getScoreAo().setImplicitScore(implicitScore);
                            nerFeatureScoreDaysAo.getScoreDaysAoList().add(entityScoreDaysAo);
                            nerFeatureScoreDaysAo.setNerType(postNerResult.getNerType());
                            nerFeatureScoreDaysAo.setKeyWord(keyWord);
                            nerFeatureScoreMap.put(keyWord, nerFeatureScoreDaysAo);
                        }
                    }
                    /// label
                    Integer label = postFeatureAo.getPostType();
                    PostTypeEnum postTypeEnum = PostTypeEnum.getByCode(label);
                    if (postTypeEnum == PostTypeEnum.OTHER){
                        continue;
                    }
                    List<ScoreDaysAo> labelScoreDaysAoList = labelScoreMap.computeIfAbsent(
                            label,
                            k -> new ArrayList<>()
                    );
                    ScoreDaysAo labelScoreDaysAo = new ScoreDaysAo();
                    labelScoreDaysAo.setDays(getDays(timestamp));
                    labelScoreDaysAo.getScoreAo().setImplicitScore(implicitScore);
                    labelScoreDaysAoList.add(labelScoreDaysAo);
                    labelScoreMap.put(label, labelScoreDaysAoList);
                }
            }
        }

        // 处理 搜索特征
        if (!CollectionUtils.isEmpty(userSearchFeature)){
            for (Object o : userSearchFeature) {
                if (o instanceof PostExplicitTimeAo){
                    for (PostExplicitPostScoreAo postExplicitPostScoreAo : ((PostExplicitTimeAo) o).getPostExplicitPostScoreAos()) {
                        Long postId = postExplicitPostScoreAo.getPostId();
                        List<ScoreDaysAo> postFeatures = postFeaturesMap.computeIfAbsent(
                                postId,
                                k -> new ArrayList<>()
                        );
                        ScoreDaysAo postScoreDaysAo = new ScoreDaysAo();
                        Long timestamp = postExplicitPostScoreAo.getTimestamp();
                        if (timestamp == null){
                            continue;
                        }
                        postScoreDaysAo.setDays(getDays(timestamp));
                        Double explicitScore = postExplicitPostScoreAo.getScore();
                        postScoreDaysAo.getScoreAo().setImplicitScore(explicitScore);
                        postFeatures.add(postScoreDaysAo);
                        postFeaturesMap.put(postId, postFeatures); // 是指针，已经存放在内存了，此行是保险

                        /// entity + label
                        PostFeatureAo postFeatureAo = postFeatureService.getPostFeature(postId);
                        if (postFeatureAo == null){
                            continue;
                        }
                        /// entity
                        if (!CollectionUtils.isEmpty(postFeatureAo.getPostNerResultList())){
                            for (PostNerResult postNerResult : postFeatureAo.getPostNerResultList()){
                                String keyWord = postNerResult.getKeyWord();
                                NerFeatureScoreDaysAo nerFeatureScoreDaysAo = nerFeatureScoreMap.computeIfAbsent(
                                        keyWord,
                                        k -> new NerFeatureScoreDaysAo()
                                );
                                ScoreDaysAo entityScoreDaysAo = new ScoreDaysAo();
                                entityScoreDaysAo.setDays(getDays(timestamp));
                                entityScoreDaysAo.getScoreAo().setImplicitScore(explicitScore);
                                nerFeatureScoreDaysAo.getScoreDaysAoList().add(entityScoreDaysAo);
                                nerFeatureScoreDaysAo.setNerType(postNerResult.getNerType());
                                nerFeatureScoreDaysAo.setKeyWord(keyWord);
                                nerFeatureScoreMap.put(keyWord, nerFeatureScoreDaysAo);
                            }
                        }
                        /// label
                        Integer label = postFeatureAo.getPostType();
                        PostTypeEnum postTypeEnum = PostTypeEnum.getByCode(label);
                        if (postTypeEnum == PostTypeEnum.OTHER){
                            continue;
                        }
                        List<ScoreDaysAo> labelScoreDaysAoList = labelScoreMap.computeIfAbsent(
                                label,
                                k -> new ArrayList<>()
                        );
                        ScoreDaysAo labelScoreDaysAo = new ScoreDaysAo();
                        labelScoreDaysAo.setDays(getDays(timestamp));
                        labelScoreDaysAo.getScoreAo().setImplicitScore(explicitScore);
                        labelScoreDaysAoList.add(labelScoreDaysAo);
                        labelScoreMap.put(label, labelScoreDaysAoList);
                    }
                }
            }
        }

        return null;
    }*/

    private int getDays(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long days = (currentTime - timestamp) / (1000L * 60 * 60 * 24);
        return (int) days;
    }



    @Override
    public UserHistoryFeatureAo getUserProfile(Long userId) {
        UserHistoryFeatureAo userHistoryFeature = new UserHistoryFeatureAo();
        for (String nodeType : FeatureTypeChanger.nerTypes){
            List<Map<String, Object>> results = userFeatureRepository.findUserRelatedEntitiesWithWeights(
                    userId,
                    FeatureTypeChanger.nerTypeToEntityLabel(nodeType),
                    FeatureTypeChanger.nerTypeToUserRelationType(nodeType)
                    );

            // 转换为UserEntityRelation对象并添加到集合
            results.forEach(result -> {
                UserEntityRelation relation = new UserEntityRelation();
                relation.setUserId(userId);
                String nodeName = (String) result.get("nodeName");
                relation.setEntityName(nodeName);
                relation.setEntityType(DiseasesKnowledgeGraphEnum.getEnumByName(nodeName).getValue());
                relation.setClickTimes((Integer) result.get("clickTimes"));
                relation.setImplicitScore((Double) result.get("implicitScore"));
                relation.setExplicitScore((Double) result.get("explicitScore"));
                relation.setLastUpdateTimestamp((Long) result.getOrDefault("lastUpdateTimestamp", System.currentTimeMillis()));

                userHistoryFeature.getUserEntityRelations().add(relation);
            });
        }
        return userHistoryFeature;
    }

    @Override
    public List<UserEntityScore> getUserProfileList(UserHistoryFeatureAo userHistoryFeatureAo) {
        List<UserEntityScore> userEntityScores = new ArrayList<>();
        if (userHistoryFeatureAo == null || CollectionUtils.isEmpty(userHistoryFeatureAo.getUserEntityRelations())){
            return userEntityScores;
        }
        for (UserEntityRelation relation : userHistoryFeatureAo.getUserEntityRelations()){
            UserEntityScore userEntityScore = new UserEntityScore();
            userEntityScore.setEntityName(relation.getEntityName());
            userEntityScore.setEntityType(relation.getEntityType());
            ScoreAo scoreAo = new ScoreAo();
            scoreAo.setClickTimes(relation.getClickTimes());
            scoreAo.setImplicitScore(relation.getImplicitScore());
            scoreAo.setExplicitScore(relation.getExplicitScore());
            double score = ruleHistoryFeature.execute(scoreAo);
            userEntityScore.setScore(score);
            userEntityScores.add(userEntityScore);
        }
        // 从大到小排序
        userEntityScores.sort((o1, o2) -> o2.getScore().compareTo(o1.getScore()));
        return userEntityScores;
    }

    @Override
    public List<UserEntityScore> getUserProfileList(Long userId) {
        UserHistoryFeatureAo userHistoryFeatureAo = getUserProfile(userId);
        return getUserProfileList(userHistoryFeatureAo);
    }

    @Override
    public List<Map<String, Double>> getUserOnlineFeature(Long userId) {
        List<Map<String, Double>> onlineFeature = new ArrayList<>();
        for (String nodeType : FeatureTypeChanger.nerTypes){
            List<Map<String, Object>> results = userFeatureRepository.findUserRelatedEntitiesWithWeights(
                    userId,
                    FeatureTypeChanger.nerTypeToEntityLabel(nodeType),
                    FeatureTypeChanger.nerTypeToUserRelationType(nodeType)
            );

            // 转换为UserEntityRelation对象并添加到集合
            results.forEach(result -> {
                String nodeName = (String) result.get("nodeName");
                ScoreAo scoreAo = new ScoreAo();
                scoreAo.setClickTimes((Integer) result.get("clickTimes"));
                scoreAo.setImplicitScore((Double) result.get("implicitScore"));
                scoreAo.setExplicitScore((Double) result.get("explicitScore"));
                double score = ruleHistoryFeature.execute(scoreAo);
                Map<String, Double> map = new HashMap<>();
                map.put(nodeName, score);
                onlineFeature.add(map);
            });
        }

        // 排序
        onlineFeature.sort((o1, o2) -> {
            double score1 = o1.values().iterator().next();
            double score2 = o2.values().iterator().next();
            return Double.compare(score2, score1);
        });

        return onlineFeature;
    }
}
