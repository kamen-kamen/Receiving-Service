//package com.waregang.receiving_service.receiving_process.infrastructure.configuration;
//
//import com.waregang.receiving_service.receiving_process.infrastructure.dto.GoodsReceiptDto;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection0.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration
//public class RedisConfiguration {
//
//    @Bean
//    public RedisTemplate<String, GoodsReceiptDto> redisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, GoodsReceiptDto> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//
//        JacksonJsonRedisSerializer<GoodsReceiptDto> jsonSerializer =
//                new JacksonJsonRedisSerializer<>(GoodsReceiptDto.class);
//
//        template.setValueSerializer(jsonSerializer);
//        template.setHashValueSerializer(jsonSerializer);
//
//        return template;
//    }
//}
//
