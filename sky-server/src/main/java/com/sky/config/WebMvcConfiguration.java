package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import com.sky.json.JacksonObjectMapper;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");
    }

    /**
     * 通过knife4j生成接口文档
     * 
     * @return
     */
    @Bean // Docket：Swagger 的核心对象，用于配置接口文档的扫描规则
    public Docket docket() {
        log.info("准备生成接口文档...");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)// 指定 Swagger 版本
                .apiInfo(apiInfo)// 注入上面设置的文档信息
                .select()// 配置接口扫描规则
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))// 扫描指定包下的接口
                .paths(PathSelectors.any())// 匹配包下所有路径
                .build();
        return docket;
    }

    /**
     * 设置静态资源映射
     * 
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射...");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        // Spring Boot 默认只处理请求路径（如 /admin/**），不直接暴露静态资源。而 Knife4j/Swagger
        // 的页面是静态的前端页面（HTML、JS、CSS），被打包在 jar 包内部，需要手动把它们"映射"出来才能访问
    }

    /*
     * 扩展spring MVC的消息转换器，添加自定义的消息转换器
     * 
     * @param converters
     */
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器....");
        // 创建消息转换器类
        MappingJackson2HttpMessageConverter Converter = new MappingJackson2HttpMessageConverter();
        // 为消息转换器设置对象转换器，将java对象序列化为json数据
        Converter.setObjectMapper(new JacksonObjectMapper());
        // 将自定义的消息转换器对象添加到spring MVC框架的转换器集合中
        converters.add(0,Converter);
    }
}
