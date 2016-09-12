package com.github.theborakompanioni.linkshortener.links;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import rx.Observable;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CachingLinkShortener implements LinkShortener {
    private Cache<Long, String> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    private final LinkShortener delegate;

    public CachingLinkShortener(LinkShortener delegate) {
        this.delegate = delegate;
    }

    public Observable<Optional<String>> get(long id) {
        return Optional.ofNullable(cache.getIfPresent(id))
                .map(foo -> Observable.just(Optional.of(foo)))
                .orElseGet(() -> delegate.get(id)
                        .doOnNext(optVal -> optVal.ifPresent(val -> cache.put(id, val))));
    }

    public Observable<Long> put(String url) {
        return delegate.put(url);
    }
}