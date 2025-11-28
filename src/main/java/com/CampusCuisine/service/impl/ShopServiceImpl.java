package com.CampusCuisine.service.impl;

import com.CampusCuisine.dto.Result;
import com.CampusCuisine.entity.Shop;
import com.CampusCuisine.mapper.ShopMapper;
import com.CampusCuisine.service.IShopService;
import com.CampusCuisine.utils.RedisConstants;
import com.CampusCuisine.utils.RedisData;
import com.CampusCuisine.utils.SystemConstants;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.print.DocFlavor.READER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;


@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
  @Autowired
  private StringRedisTemplate stringRedisTemplate;
  private static ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

  @Override
  public Result queryById(Long id) {
    Shop shop = quaryWitShopPassThough(id);
    if (shop == null) {
      return Result.fail("店铺不存在！");
    }

    return Result.ok(shop);
  }

  // 逻辑过期
  public Shop quatyWithLogicalExpire(Long id) {
    String key = RedisConstants.CACHE_SHOP_KEY + id;
    String shopJson = stringRedisTemplate.opsForValue().get(key);
    if (StrUtil.isBlank(shopJson)) {
      return null;
    }
    RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
    Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
    LocalDateTime expireTime = redisData.getExpireTime();
    if (expireTime.isAfter(LocalDateTime.now())) {
      return shop;
    }

    String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
    boolean isLock = tryLock(lockKey);
    if (isLock) {
      CACHE_REBUILD_EXECUTOR.submit(() -> {
        try {
          this.saveDatetoRedis(id, 20L);
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
          unLock(lockKey);
        }

      });
    }
    return shop;

  }

  // 互斥锁
  public Shop quaryWithMutex(Long id) {
    String key = RedisConstants.CACHE_SHOP_KEY + id;
    String shopJson = stringRedisTemplate.opsForValue().get(key);
    if (StrUtil.isNotBlank(shopJson)) {// ""和null都会false
      return JSONUtil.toBean(shopJson, Shop.class);
    }
    if (shopJson != null)

    {
      return null;
    }
    Shop shop = null;
    String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
    try {

      Boolean isLock = tryLock(lockKey);
      if (!isLock) {
        Thread.sleep(50);
        return quaryWithMutex(id);
      }
      shop = getById(id);
      if (shop == null) {
        stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_SHOP_TTL,
            TimeUnit.MINUTES);
        return null;
      }
      stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL,
          TimeUnit.MINUTES);
    } catch (Exception e) {
      // TODO: handle exception
    } finally {
      unLock(lockKey);
    }
    return shop;
  }

  // 基本逻辑
  private Shop quaryWitShopPassThough(Long id) {
    String key = RedisConstants.CACHE_SHOP_KEY + id;
    String shopJson = stringRedisTemplate.opsForValue().get(key);
    if (StrUtil.isNotBlank(shopJson)) {
      return JSONUtil.toBean(shopJson, Shop.class);
    }
    if (shopJson != null) {
      return null;
    }
    Shop shop = getById(id);
    if (shop == null) {
      stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_SHOP_TTL,
          TimeUnit.MINUTES);
      return null;
    }
    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL,
        TimeUnit.MINUTES);
    return shop;
  }

  private boolean tryLock(String key) {
    boolean isLock = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
    return BooleanUtil.isTrue(isLock);
  }

  private void unLock(String key) {
    stringRedisTemplate.delete(key);
  }

  private void saveDatetoRedis(Long id, Long expireSeconds) {
    Shop shop = getById(id);
    RedisData redisData = new RedisData();
    redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
    redisData.setData(redisData);
    stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
  }

  @Override
  public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
    if (x == null || y == null) {
            // 不需要坐标查询，按数据库查询
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            // 返回数据
            return Result.ok(page.getRecords());
        }
      int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
      int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
      String key = RedisConstants.SHOP_GEO_KEY + typeId;
      GeoResults<RedisGeoCommands.GeoLocation<String>> results =
        stringRedisTemplate.opsForGeo().radius(
                key,
                new Circle(new Point(x, y), new Distance(5000)),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(end)
        );


      // GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo() // GEOSEARCH key BYLONLAT x y BYRADIUS 10 WITHDISTANCE
      //           .search(
      //                   key,
      //                   GeoReference.fromCoordinate(x, y),
      //                   new Distance(5000),
      //                   RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
      //           );
        // 4.解析出id
        if (results == null) {
            return Result.ok(Collections.emptyList());
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if (list.size() <= from) {
            // 没有下一页了，结束
            return Result.ok(Collections.emptyList());
        }
        // 4.1.截取 from ~ end的部分
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(result -> {
            // 4.2.获取店铺id
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            // 4.3.获取距离
            Distance distance = result.getDistance();
            distanceMap.put(shopIdStr, distance);
        });
        // 5.根据id查询Shop
        String idStr = StrUtil.join(",", ids);
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Shop shop : shops) {
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        // 6.返回
        return Result.ok(shops);
      // return Result.ok();
  }
}
