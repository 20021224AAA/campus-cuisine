package com.CampusCuisine.utils;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.CampusCuisine.entity.Shop;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CatchClient {
  private static StringRedisTemplate stringRedisTemplate;

  public CatchClient(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  public void set(String key, Object value, Long time, TimeUnit unit) {
    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
  }

  public void setWithLOgicalExpire(String key, Object value, Long time, TimeUnit unit) {
    RedisData redisData = new RedisData();
    redisData.setData(value);
    redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
  }

  public <R, ID> R quaryWitShopPassThough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback) {
    String key = keyPrefix + id;
    String json = stringRedisTemplate.opsForValue().get(key);
    if (StrUtil.isNotBlank(json)) {
      return JSONUtil.toBean(json, type);
    }
    if (json != null) {
      return null;
    }
    R r = dbFallback.apply(id);
    if (r == null) {
      stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_SHOP_TTL,
          TimeUnit.MINUTES);
      return null;
    }
    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(r), RedisConstants.CACHE_SHOP_TTL,
        TimeUnit.MINUTES);
    return r;
  }

}
