package com.github.theborakompanioni.linkshortener.links;

import com.google.common.collect.Maps;
import rx.Observable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class MapLinkShortener implements LinkShortener {
    private Map<Long, String> cache = Maps.newConcurrentMap();

    public Observable<Optional<String>> get(long id) {
        return Observable.just(Optional.ofNullable(cache.get(id)));
    }

    public Observable<Long> put(String url) {
        long key = createKey();
        cache.put(key, url);
        return Observable.just(key);
    }

    private long createKey() {
        long key = ThreadLocalRandom.current().nextLong();
        while (cache.containsKey(key)) {
            key = ThreadLocalRandom.current().nextLong();
        }
        return key;
    }
}