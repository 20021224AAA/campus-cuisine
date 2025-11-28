package com.CampusCuisine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;


import com.CampusCuisine.mapper.ShopMapper;
import com.CampusCuisine.service.IShopService;
import com.CampusCuisine.service.impl.ShopServiceImpl;
import com.CampusCuisine.utils.RedisConstants;
import com.CampusCuisine.utils.RedisIdWorker;
import com.CampusCuisine.entity.Shop;

import cn.hutool.log.Log;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@SpringBootTest
public class CampusCuisineTest {
  @Resource
  private ShopServiceImpl shopService;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @org.junit.jupiter.api.Test
  public void loadShopData() {
    
  List<Shop> list = shopService.list();
  Map<Long, List<Shop>> map =
  list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
  for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {
  Long typeId = entry.getKey();
  String key = RedisConstants.SHOP_GEO_KEY + typeId;
  List<Shop> value = entry.getValue();
  List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>();
  for (Shop shop : value) {
  locations.add(
  new RedisGeoCommands.GeoLocation<String>(shop.getId().toString(), new
  Point(shop.getX(), shop.getY())));
  }
  stringRedisTemplate.opsForGeo().add(key, locations);
  }

  }
}
