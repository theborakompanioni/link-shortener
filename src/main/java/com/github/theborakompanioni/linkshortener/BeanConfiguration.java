package com.github.theborakompanioni.linkshortener;

import com.github.theborakompanioni.linkshortener.links.CachingLinkShortener;
import com.github.theborakompanioni.linkshortener.links.RedisLinkShortener;
import com.lambdaworks.redis.RedisClient;
import io.vertx.rxjava.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.embedded.RedisServer;

@Configuration
class BeanConfiguration {

    private AppConfiguration appConfiguration;

    @Autowired
    public BeanConfiguration(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    @Bean
    public HttpServer httpServer() {
        return new HttpServer(appConfiguration,
                staticsApi(), linkShortenerApi());
    }

    @Bean
    public LinkShortenerApi linkShortenerApi() {
        return new LinkShortenerApi(rxVertx(), redisLinkShortener());
    }

    @Bean
    public StaticsApi staticsApi() {
        return new StaticsApi(rxVertx());
    }


    @Bean
    public Vertx rxVertx() {
        return Vertx.vertx();
    }

    @Bean
    public io.vertx.core.Vertx vertx() {
        return (io.vertx.core.Vertx) rxVertx().getDelegate();
    }


    @Bean(destroyMethod = "stop")
    public RedisServer redisServer() {
        RedisServer redisServer = RedisServer.builder()
                .port(appConfiguration.redisPort())
                .build();

        redisServer.start();

        return redisServer;
    }

    @Bean
    public RedisClient redisClient() {
        final RedisClient redisClient = RedisClient.create("redis://localhost:" + appConfiguration.redisPort());
        return redisClient;
    }

    @Bean
    public RedisLinkShortener redisLinkShortener() {
        return new RedisLinkShortener(redisClient());
    }

    @Primary
    @Bean
    public CachingLinkShortener CachingLinkShortener() {
        return new CachingLinkShortener(redisLinkShortener());
    }
}
