package com.CampusCuisine.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Converter {

    @Bean
    public MessageConverter jacksonMessageConvertor(){
        Jackson2JsonMessageConverter jjmc = new Jackson2JsonMessageConverter();
        jjmc.setCreateMessageIds(true);
        return jjmc;
    }
}
