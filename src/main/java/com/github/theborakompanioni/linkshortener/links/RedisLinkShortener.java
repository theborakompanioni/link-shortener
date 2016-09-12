package com.github.theborakompanioni.linkshortener.links;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.rx.RedisReactiveCommands;
import rx.Observable;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class RedisLinkShortener implements LinkShortener {

    private RedisClient redisClient;

    public RedisLinkShortener(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public Observable<Optional<String>> get(long id) {
        return redisClient.connect().reactive().get(String.valueOf(id))
                .map(Optional::ofNullable)
                .switchIfEmpty(Observable.just(Optional.empty()));
    }

    public Observable<Long> put(String url) {
        final RedisReactiveCommands<String, String> reactive = redisClient.connect().reactive();
        long key = createKey(reactive);
        String strKey = String.valueOf(key);

        return reactive.set(strKey, url)
                .doOnCompleted(
                        () -> reactive.expireat(strKey, Date.valueOf(LocalDate.now().plusYears(1)))
                ).map(foo -> key);
    }

    private long createKey(RedisReactiveCommands<String, String> reactive) {
        long key = ThreadLocalRandom.current().nextLong();
        while (reactive.exists(String.valueOf(key)).toBlocking().single() > 0) {
            key = ThreadLocalRandom.current().nextLong();
        }
        return key;
    }
}