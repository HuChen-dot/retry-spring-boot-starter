package com.retry.annotation;

import com.retry.config.RetryableConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({RetryableConfig.class})
public @interface EnableRetryable {
}
