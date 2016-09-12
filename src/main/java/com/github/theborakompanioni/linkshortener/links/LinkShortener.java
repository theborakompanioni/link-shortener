package com.github.theborakompanioni.linkshortener.links;

import rx.Observable;

import java.util.Optional;

public interface LinkShortener {
    Observable<Optional<String>> get(long id);

    Observable<Long> put(String url);
}
