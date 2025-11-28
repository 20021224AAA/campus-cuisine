package com.CampusCuisine.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.CampusCuisine.utils.LoginInterceptor;
import com.CampusCuisine.utils.RefreshTokenInterceptor;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LoginInterceptor())
        .excludePathPatterns(
            "/shop/**",
            " /voucher/** ",
            "/shop-type/**",
            "/upload/**",
            "/blog/hot",
            "/user/code",
            "/user/login")
        .order(1);
    registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
  }

}
