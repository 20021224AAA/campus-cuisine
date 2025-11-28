package com.CampusCuisine.utils;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class SimpleRedisLock implements ILock {
  private StringRedisTemplate stringRedisTemplate;
  private String name;
  private static final String key_prefix = "lock:";
  private static final String ID_prefix = UUID.randomUUID().toString() + "-";
  private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
  static {
    UNLOCK_SCRIPT = new DefaultRedisScript<>();
    UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
    UNLOCK_SCRIPT.setResultType(Long.class);
  }

  public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
    this.name = name;
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Override
  public boolean tryLock(long timeoutSec) {
    String threadId = ID_prefix + Thread.currentThread().getId();
    Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key_prefix + name, threadId + "", timeoutSec,
        TimeUnit.SECONDS);
    System.err.println("SUCCESS LOCK!");
    return Boolean.TRUE.equals(success);
  }

  public void unLock() {
    stringRedisTemplate.execute(UNLOCK_SCRIPT,
        Collections.singletonList(key_prefix + name), ID_prefix + Thread.currentThread().getId());
  }
  // @Override
  // public void unLock() {

  // // 分布式锁，方式不同JVM的线程Id冲突
  // String threadId = ID_prefix + Thread.currentThread().getId();
  // String id = stringRedisTemplate.opsForValue().get(key_prefix + name);
  // if (threadId.equals(id)) {
  // stringRedisTemplate.delete(key_prefix + name);
  // }

  // }

}
