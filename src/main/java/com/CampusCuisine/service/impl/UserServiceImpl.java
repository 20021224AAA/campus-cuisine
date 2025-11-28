package com.CampusCuisine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.CampusCuisine.dto.LoginFormDTO;
import com.CampusCuisine.dto.Result;
import com.CampusCuisine.dto.UserDTO;
import com.CampusCuisine.entity.User;
import com.CampusCuisine.mapper.UserMapper;
import com.CampusCuisine.service.IUserService;
import com.CampusCuisine.utils.RedisConstants;
import com.CampusCuisine.utils.RegexUtils;
import com.CampusCuisine.utils.SystemConstants;
import com.CampusCuisine.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Override
  public Result sendCode(String phone, HttpSession session) {
    if (RegexUtils.isPhoneInvalid(phone)) {
      return Result.fail("手机号码格式错误！");
    }
    String code = RandomUtil.randomNumbers(6);
    stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL,
        TimeUnit.MINUTES);
    // session.setAttribute("code", code);
    log.info("发送短信验证码成功，验证码：{}", code);
    return Result.ok();
  }

  @Override
  public Result login(LoginFormDTO loginForm, HttpSession session) {
    String phone = loginForm.getPhone();
    if (RegexUtils.isPhoneInvalid(phone)) {
      return Result.fail("手机号码格式错误！");
    }
    String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);

    // Object cacheCode = session.getAttribute("code");
    String code = loginForm.getCode();
    if (cacheCode == null || !cacheCode.equals(code)) {
      return Result.fail("验证码错误");
    }
    User user = query().eq("phone", phone).one();
    if (user == null) {
      user = createUserWithPhone(phone);
    }

    String token = UUID.randomUUID().toString();

    UserDTO userDTO = new UserDTO();
    BeanUtils.copyProperties(user, userDTO);
    // Map<String, Object> userMap = BeanUtil.beanToMap(userDTO);
    Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
        CopyOptions.create().setIgnoreNullValue(true)
            .setFieldValueEditor((fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString()));

    String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
    stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
    stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
    // session.setAttribute("user", userDTO);
    return Result.ok(token);
  }

  private User createUserWithPhone(String phone) {
    User user = new User();
    user.setPhone(phone);
    user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
    save(user);
    return user;
  }

  @Override
  public Result sign() {
    Long userId=UserHolder.getUser().getId();
    LocalDateTime now=LocalDateTime.now();
    String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
    String key = RedisConstants.USER_SIGN_KEY + userId + keySuffix;
    int dayOfMonth = now.getDayOfMonth();
    // 5.写入Redis SETBIT key offset 1
    stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
    return Result.ok();
  }

  @Override
  public Result signCount() {
    // 1.获取当前登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.获取日期
        LocalDateTime now = LocalDateTime.now();
        // 3.拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = RedisConstants.USER_SIGN_KEY + userId + keySuffix;
        // 4.获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        // 5.获取本月截止今天为止的所有的签到记录，返回的是一个十进制的数字 BITFIELD sign:5:202203 GET u14 0
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );
        if (result == null || result.isEmpty()) {
            // 没有任何签到结果
            return Result.ok(0);
        }
        Long num = result.get(0);
        if (num == null || num == 0) {
            return Result.ok(0);
        }
        // 6.循环遍历
        int count = 0;
        while (true) {
            // 6.1.让这个数字与1做与运算，得到数字的最后一个bit位  // 判断这个bit位是否为0
            if ((num & 1) == 0) {
                // 如果为0，说明未签到，结束
                break;
            }else {
                // 如果不为0，说明已签到，计数器+1
                count++;
            }
            // 把数字右移一位，抛弃最后一个bit位，继续下一个bit位
            num >>>= 1;
        }
        return Result.ok(count);
  
  
  
  }

}
