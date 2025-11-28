package com.CampusCuisine.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.CampusCuisine.dto.Result;
import com.CampusCuisine.entity.Voucher;
import com.CampusCuisine.mapper.VoucherMapper;
import com.CampusCuisine.entity.SeckillVoucher;
import com.CampusCuisine.service.ISeckillVoucherService;
import com.CampusCuisine.service.IVoucherService;
import com.CampusCuisine.utils.RedisConstants;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        if(vouchers!=null){
          for(Voucher voucher:vouchers){
            boolean isexit=stringRedisTemplate.hasKey(RedisConstants.SECKILL_STOCK_KEY + voucher.getId());
            if(!isexit&&voucher.getType()==1){
            stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY + voucher.getId(),voucher.getStock().toString());
            }  
        }  
        }
        
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY + voucher.getId(),
                voucher.getStock().toString());
    }
}
