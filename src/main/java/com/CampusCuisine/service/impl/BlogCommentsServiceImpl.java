package com.CampusCuisine.service.impl;

import com.CampusCuisine.entity.BlogComments;
import com.CampusCuisine.mapper.BlogCommentsMapper;
import com.CampusCuisine.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
