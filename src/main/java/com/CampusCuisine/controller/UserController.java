package com.CampusCuisine.controller;

import com.CampusCuisine.dto.LoginFormDTO;
import com.CampusCuisine.dto.Result;
import com.CampusCuisine.dto.UserDTO;
import com.CampusCuisine.entity.UserInfo;
import com.CampusCuisine.service.IUserInfoService;
import com.CampusCuisine.service.IUserService;
import com.CampusCuisine.utils.RedisConstants;
import com.CampusCuisine.utils.UserHolder;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 发送手机验证码
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // TODO 发送短信验证码并保存验证码
        return userService.sendCode(phone, session);
    }

    /**
     * 登录功能
     * 
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session) {
        // TODO 实现登录功能
        return userService.login(loginForm, session);
    }

    /**
     * 登出功能
     * 
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            return Result.fail("成功退出！");
        }
        String key=RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.delete(key);
        UserHolder.removeUser();
        // TODO 实现登出功能
        return Result.fail("成功退出！");
    }

    @GetMapping("/me")
    public Result me() {
        UserDTO user = UserHolder.getUser();
        log.info("当前用户：", user);
        return Result.ok(user);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId) {
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }
    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }
}
