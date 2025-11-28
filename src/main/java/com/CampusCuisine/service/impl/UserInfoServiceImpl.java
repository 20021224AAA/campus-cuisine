package com.CampusCuisine.service.impl;

import com.CampusCuisine.entity.UserInfo;
import com.CampusCuisine.mapper.UserInfoMapper;
import com.CampusCuisine.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
