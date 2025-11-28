package com.CampusCuisine.service;

import com.CampusCuisine.entity.Follow;

import com.baomidou.mybatisplus.extension.service.IService;
import com.CampusCuisine.dto.Result;

public interface IFollowService extends IService<Follow> {

    Result follow(Long followUserId, Boolean isFollow);

    Result followCommons(Long id);

}
