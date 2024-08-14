package com.beyond.ordersystem.common.configs;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
@Configuration
public class RedisConfig {

    //  @Value 어노테이션
    //  application.yml 의 spring.redis.host 의 정보를 소스코드의 변수로 가져오는 것이다.

    @Value("${spring.redis.host}")
    public String host;

    @Value("${spring.redis.port}")
    public int port;

    @Bean
    @Qualifier("2")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(this.host);
        redisStandaloneConfiguration.setPort(this.port);
        redisStandaloneConfiguration.setDatabase(1);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    @Qualifier("2")
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("2") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
    //  redisTemplate.opsForValue().set(key. value)
    //  redisTemplate.opsForValue().get(key)
    //  redisTemplate.opsForValue().increment 또는 decrement


    @Bean
    @Qualifier("3")
    public RedisConnectionFactory redisStockFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(this.host);
        redisStandaloneConfiguration.setPort(this.port);
        redisStandaloneConfiguration.setDatabase(2);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    @Qualifier("3")
    public RedisTemplate<String, Object> redisStockTemplate(@Qualifier("3") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }


    @Bean
    @Qualifier("4")
    public RedisConnectionFactory SseFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(this.host);
        redisStandaloneConfiguration.setPort(this.port);
        redisStandaloneConfiguration.setDatabase(3);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    @Qualifier("4")
    public RedisTemplate<String, Object> sseRedisTemplate(@Qualifier("4") RedisConnectionFactory sseFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        //  객체안의 객체 직렬화 이슈로 인해 아래와 같이 serializer 커스텀
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        serializer.setObjectMapper(objectMapper);
        redisTemplate.setValueSerializer(serializer);

        redisTemplate.setConnectionFactory(sseFactory);
        return redisTemplate;
    }

    //  리스너 객체 생성
    @Bean
    @Qualifier("4")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("4") RedisConnectionFactory sseFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(sseFactory);
        return container;
    }
}