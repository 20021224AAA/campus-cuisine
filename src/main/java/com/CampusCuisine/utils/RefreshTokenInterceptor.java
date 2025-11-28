package com.CampusCuisine.utils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import com.CampusCuisine.dto.UserDTO;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;

public class RefreshTokenInterceptor implements HandlerInterceptor {
  private StringRedisTemplate stringRedisTemplate;

  public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String token = request.getHeader("authorization");
    if (StrUtil.isBlank(token)) {
      return true;
    }
    String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
    Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);

    if (userMap.isEmpty()) {
      return true;
    }
    UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
    UserHolder.saveUser(userDTO);
    stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL,
        TimeUnit.MINUTES);
    return true;
  }
}
