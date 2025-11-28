package com.CampusCuisine.service;

import com.CampusCuisine.dto.Result;
import com.CampusCuisine.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IShopService extends IService<Shop> {

  Result queryById(Long id);

  Result queryShopByType(Integer typeId, Integer current, Double x, Double y);

}
