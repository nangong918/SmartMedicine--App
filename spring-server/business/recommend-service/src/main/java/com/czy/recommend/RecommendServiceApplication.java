package com.czy.recommend;

import com.czy.springUtils.debug.DebugConfig;
import com.czy.springUtils.start.PortApplicationContextInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author 13225
 * @date 2025/1/10 18:25
 */
@EnableConfigurationProperties(DebugConfig.class)
@SpringBootApplication(
        // 扫描指定包下的类
        scanBasePackages = {
                // 扫描api模块
                "com.czy.api",
                // 扫描本模块
                "com.czy.recommend",
                // 扫描工具类
                "com.utils.mvc"
        },
        // 排除
        exclude = {}
)
public class RecommendServiceApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(RecommendServiceApplication.class)
                .initializers(new PortApplicationContextInitializer())
                .run(args);
    }
}
