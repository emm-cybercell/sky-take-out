package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component// 将当前类交给 Spring 容器进行管理，成为一个 Bean 对象，Spring 启动时会扫描到该类，
          //自动创建它的实例并放入 IoC容器中，其他地方可以直接 @Autowired 注入使用。
@ConfigurationProperties(prefix = "sky.jwt")// 将配置文件中以 sky.jwt 开头的属性值，自动绑定到这个类的字段上
@Data//Lombok注解，自动生成 getter/setter/toString/equals/hashCode 方法，简化代码编写
public class JwtProperties {

    /**
     * 管理端员工生成jwt令牌相关配置
     */
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

    /**
     * 用户端微信用户生成jwt令牌相关配置
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;

}
