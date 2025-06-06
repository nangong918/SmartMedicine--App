package com.czy.message.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoDBConfig {

//    @Bean // 目的，就是为了移除 _class field 。参考博客 https://blog.csdn.net/bigtree_3721/article/details/82787411
//    public MappingMongoConverter mappingMongoConverter(MongoDbFactory factory,
//                                                       MongoMappingContext context,
//                                                       BeanFactory beanFactory) {
//        // 创建 DbRefResolver 对象
//        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
//        // 创建 MappingMongoConverter 对象
//        MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
//        // 设置 conversions 属性
//        try {
//            mappingConverter.setCustomConversions(beanFactory.getBean(CustomConversions.class));
//        } catch (NoSuchBeanDefinitionException ignore) {
//        }
//        // 设置 typeMapper 属性，从而移除 _class field 。
//        mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
//        return mappingConverter;
//    }

    @Bean(name = "mongoTemplate")
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory, MongoMappingContext mongoMappingContext) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);
        MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        //去掉_class字段
        mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(mongoDatabaseFactory,mappingConverter);
    }

}
