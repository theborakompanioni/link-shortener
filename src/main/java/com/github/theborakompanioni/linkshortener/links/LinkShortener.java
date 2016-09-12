package com.github.theborakompanioni.linkshortener.links;

import rx.Observable;

import java.util.Optional;

/**
 * Created by void on 9/13/16.
 */
public interface LinkShortener {
    Observable<Optional<String>> get(long id);

    Observable<Long> put(String url);
}
