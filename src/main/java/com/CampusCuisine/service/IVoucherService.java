package com.CampusCuisine.service;

import com.CampusCuisine.dto.Result;
import com.CampusCuisine.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);
}
