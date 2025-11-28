package com.CampusCuisine.utils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import com.CampusCuisine.dto.UserDTO;
import com.CampusCuisine.entity.User;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;

public class LoginInterceptor implements HandlerInterceptor {
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (UserHolder.getUser() == null) {
      response.setStatus(401);
      return false;

    }
    return true;

    // HttpSession session = request.getSession();
    // Object user = session.getAttribute("user");
    // String token = request.getHeader("authorization");
    // if (StrUtil.isBlank(token)) {
    // response.setStatus(401);
    // return false;
    // }
    // String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
    // Map<Object, Object> userMap =
    // stringRedisTemplate.opsForHash().entries(tokenKey);

    // if (userMap.isEmpty()) {
    // response.setStatus(401);
    // return false;
    // }
    // UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
    // UserHolder.saveUser(userDTO);
    // stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL,
    // TimeUnit.MINUTES);
    // return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    UserHolder.removeUser();

  }
}
