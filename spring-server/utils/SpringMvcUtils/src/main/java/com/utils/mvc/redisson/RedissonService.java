package com.utils.mvc.redisson;


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 13225
 * @date 2025/3/29 15:52
 */
public interface RedissonService {

    //---------------------分布式锁---------------------

    boolean tryLock(RedissonClusterLock redisLock);

    void unlock(RedissonClusterLock redisLock);

    //---------------------Key---------------------
    /**
     * 设置键的过期时间
     *
     * @param key     键
     * @param timeout 过期时间（秒）
     * @return 是否成功设置过期时间
     */
    boolean expireKey(String key, long timeout);

    /**
     * 获取指定键的剩余过期时间
     *
     * @param key 键
     * @return 剩余过期时间（秒）
     * @throws Exception 键不存在的情况
     */
    long getExpireKey(String key) throws Exception;

    /**
     * 检查指定键是否存在
     *
     * @param key 键
     * @return true 如果存在，false 如果不存在
     */
    boolean hasKey(String key);

    /**
     * 移除指定键的过期时间
     *
     * @param key 键
     * @throws Exception 键不存在的情况
     * @return 是否成功移除过期时间
     */
    boolean persist(String key) throws Exception;

    /**
     * 获取所有匹配给定模式的键
     * @param pattern   模式
     * @return          匹配的键
     */
    Collection<String> getKeysByPattern(String pattern);

    //---------------------Base---------------------

    void setBoolean(String key, Boolean value, Long expireTimes);
    boolean getBoolean(String key);

    void setString(String key, String value, Long expireTimes);
    String getString(String key);

    void setInteger(String key, Integer value, Long expireTimes);
    Integer getInteger(String key);
    // 自增
    Integer incrementInteger(String key, Integer increment, Long expireTime);

    //---------------------Object---------------------

    /**
     * 设置对象
     * @param key
     * @param obj
     * @param expireTimes
     * @return
     */
    boolean setObjectByJson(String key, Object obj, Long expireTimes);

    /**
     * 设置对象
     * @param key
     * @param obj
     * @param expireTimes
     * @return
     */
    boolean setObjectBySerializable(String key, Object obj, Long expireTimes);

    /**
     * 获取对象
     * @param key
     * @param clazz
     * @return
     * @param <T>
     */
    <T> T getObjectFromJson(String key, Class<T> clazz);

    /**
     * 获取对象
     * @param key
     * @param clazz
     * @return
     * @param <T>
     */
    <T> T getObjectFromSerializable(String key, Class<T> clazz);

    // 删除对象和key
    boolean deleteObject(String key);

    //---------------------Hash---------------------
    void saveHashMap(String key, HashMap<String, String> data, Long expireTimes);
    void saveObjectHashMap(String key, HashMap<String, Object> data, Long expireTimes);
    HashMap<String, String> getHashMap(String key);
    HashMap<String, Object> getObjectHaseMap(String key);
    Object getObjectFromHashMap(String key, String hashKey);
    void updateHashMap(String hashKey, String field, String value);
    void updateObjectHashMap(String hashKey, String field, Object value);
    void deleteHashMap(String redisKey); // 删除整个 Hash
    void deleteFieldFromHash(String redisKey, String hashKey);
    void deleteFieldFromObjectHashMap(String redisKey, String hashKey);


    // ===================== 有序集合(ZSet)操作 =====================

    /**
     * 添加元素到有序集合
     * @param key 键
     * @param value 值
     * @param score 分数
     * @param expireTime 过期时间(秒)，null表示使用默认过期时间
     * @return 是否添加成功
     */
    boolean zAdd(String key, Object value, double score, Long expireTime);

    /**
     * 批量添加元素到有序集合
     * @param key 键
     * @param values 值-分数映射
     * @param expireTime 过期时间(秒)，null表示使用默认过期时间
     * @return 添加成功的数量
     */
    int zAddAll(String key, Map<Object, Double> values, Long expireTime);

    /**
     * 获取某个元素的分数
     * @param key
     * @param member
     * @return
     */
    Double zScore(String key, Object member);

    /**
     * 批量获取元素的分数
     * @param key
     * @param members
     * @return
     */
    Map<Object, Double> zScores(String key, Collection<Object> members);

    /**
     * 从有序集合中移除元素
     * @param key 键
     * @param expireTime 过期时间(秒)，null表示使用默认过期时间
     * @param values 要移除的值
     * @return 是否移除
     */
    boolean zRemove(String key, Long expireTime, Object... values);

    /**
     * 获取有序集合中元素的分数
     * @param key 键
     * @param value 值
     * @return 分数，如果元素不存在返回null
     */
    Double zGetScore(String key, Object value);

    /**
     * 更新有序集合中元素的分数
     * @param key 键
     * @param value 值
     * @param newScore 新分数
     * @param expireTime 过期时间(秒)，null表示使用默认过期时间
     * @return 是否更新成功
     */
    boolean zUpdateScore(String key, Object value, double newScore, Long expireTime);

    /**
     * 获取有序集合的大小
     * @param key 键
     * @return 集合大小
     */
    int zSize(String key);

    /**
     * 获取有序集合中元素的排名(按分数从小到大)
     * @param key 键
     * @param value 值
     * @return 排名(从0开始)，如果元素不存在返回-1
     */
    int zRank(String key, Object value);

    /**
     * 获取有序集合中元素的排名(按分数从大到小)
     * @param key 键
     * @param value 值
     * @return 排名(从0开始)，如果元素不存在返回-1
     */
    int zReverseRank(String key, Object value);

    /**
     * 获取有序集合中指定排名范围的元素(按分数从小到大)
     * @param key 键
     * @param start 开始排名(包含)
     * @param end 结束排名(包含)
     * @return 元素集合
     */
    Collection<Object> zRange(String key, int start, int end);

    /**
     * 获取有序集合中指定排名范围的元素(按分数从大到小)
     * @param key 键
     * @param start 开始排名(包含)
     * @param end 结束排名(包含)
     * @return 元素集合
     */
    Collection<Object> zReverseRange(String key, int start, int end);

    /**
     * 获取有序集合中指定分数范围的元素
     * @param key 键
     * @param minScore 最小分数(包含)
     * @param maxScore 最大分数(包含)
     * @return 元素集合
     */
    Collection<Object> zRangeByScore(String key, double minScore, double maxScore);

    /**
     * 获取有序集合中指定分数范围的元素(带分数)
     * @param key 键
     * @param minScore 最小分数(包含)
     * @param maxScore 最大分数(包含)
     * @return 元素-分数映射
     */
    Map<Object, Double> zRangeByScoreWithScores(String key, double minScore, double maxScore);

    /**
     * 移除有序集合中指定排名范围的元素
     * @param key 键
     * @param start 开始排名(包含)
     * @param end 结束排名(包含)
     * @param expireTime 过期时间(秒)，null表示使用默认过期时间
     * @return 移除的数量
     */
    int zRemoveRangeByRank(String key, int start, int end, Long expireTime);

    /**
     * 移除有序集合中指定分数范围的元素
     * @param key 键
     * @param minScore 最小分数(包含)
     * @param maxScore 最大分数(包含)
     * @param expireTime 过期时间(秒)，null表示使用默认过期时间
     * @return 移除的数量
     */
    int zRemoveRangeByScore(String key, double minScore, double maxScore, Long expireTime);

    /**
     * 清空有序集合
     * @param key 键
     * @param expireTime 过期时间(秒)，null表示使用默认过期时间
     * @return 是否清空成功
     */
    boolean zClear(String key, Long expireTime);

    /**
     * 获取ZSet的元素数量（不加载元素到Java内存）
     * @param key ZSet的key
     * @return 元素数量
     */
    long zCount(String key);

    /**
     * 原子性地获取并删除ZSet中前N个元素（从大到小）
     * @param key ZSet的key
     * @param n 要获取的元素数量
     * @return 获取到的元素列表
     */
    List<Object> zPopTopNAndRemove(String key, int n);
}
