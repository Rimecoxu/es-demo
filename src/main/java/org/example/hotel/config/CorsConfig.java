package org.example.hotel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: Rimecoxu@gmail.com
 * @CreateTime: 2025-10-21 18:38
 * @Description: 跨域配置
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 拦截所有路径
                .allowedOrigins("*")
                // .allowedOrigins("http://localhost:3000", "https://your-app.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // .allowCredentials(true) // 是否允许携带 cookie，⚠️ 注意：这里不能同时设为 true！
                .maxAge(3600); // 预检请求缓存时间（秒）
    }
}
