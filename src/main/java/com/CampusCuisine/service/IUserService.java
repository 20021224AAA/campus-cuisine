package com.CampusCuisine.service;

import javax.servlet.http.HttpSession;

import com.baomidou.mybatisplus.extension.service.IService;
import com.CampusCuisine.dto.LoginFormDTO;
import com.CampusCuisine.dto.Result;
import com.CampusCuisine.entity.User;


public interface IUserService extends IService<User> {
  Result sendCode(String phone, HttpSession session);

  Result login(LoginFormDTO loginForm, HttpSession session);

  Result sign();

  Result signCount();

}
