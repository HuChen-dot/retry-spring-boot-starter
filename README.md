引入依赖

        <dependency>
            <groupId>com.retry</groupId>
            <artifactId>retry-spring-boot-starter</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>

在项目启动类上添加启动注解：

@EnableRetryable

在需要重试的方法上添加注解：

@Retryable

注意事项：
重试机制依赖于SpringAOP机制，需要注意Spring的代理机制；
使用时注意事项和@Transactional注解一样