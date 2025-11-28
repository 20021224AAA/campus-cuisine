package com.CampusCuisine.service;

import com.CampusCuisine.dto.Result;
import com.CampusCuisine.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IBlogService extends IService<Blog> {

    Result likeBlog(Long id);

    Result saveBlog(Blog blog);

    Result queryBlogOfFollow(Long max, Integer offset);

    Result queryBlogById(Long id);

    Result queryBlogLikes(Long id);

}
