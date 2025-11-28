package com.CampusCuisine.service;

import com.CampusCuisine.dto.Result;
import com.CampusCuisine.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IVoucherOrderService extends IService<VoucherOrder> {

  Result seckillVoucher(Long voucherId);

}
