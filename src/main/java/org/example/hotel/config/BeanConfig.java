package org.example.hotel.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Rimecoxu@gmail.com
 * @CreateTime: 2025-10-21 18:07
 * @Description: 配置类
 */
@Configuration
public class BeanConfig {

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        // 创建ES客户端
        return new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.99.99:9200")
        ));
    }
}
