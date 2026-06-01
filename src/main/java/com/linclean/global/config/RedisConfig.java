package com.linclean.global.config;

import org.springframework.context.annotation.Configuration;

/**
 * spring-boot-starter-data-redis 자동 구성으로 StringRedisTemplate 등록됨.
 * application.yml의 spring.data.redis.* 설정으로 연결.
 * 운영 전환 시 Sentinel/Cluster 설정을 이 클래스에 추가.
 */
@Configuration
public class RedisConfig {}
