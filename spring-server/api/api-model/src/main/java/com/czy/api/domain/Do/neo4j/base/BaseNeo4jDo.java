package com.czy.api.domain.Do.neo4j.base;

import lombok.Data;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;

/**
 * @author 13225
 * @date 2025/5/12 17:15
 */
@Data
public abstract class BaseNeo4jDo {
    @Id
    // 经过测试，id必须交给neo4j生成，如果需要userId则另外设置字段
    @GeneratedValue
    private Long id;
    @Property("name")
    private String name;
    public abstract String getNodeLabel();
}
