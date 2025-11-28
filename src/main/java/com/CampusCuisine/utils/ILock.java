package com.CampusCuisine.utils;

public interface ILock {
  boolean tryLock(long timeoutSec);

  void unLock();
}
