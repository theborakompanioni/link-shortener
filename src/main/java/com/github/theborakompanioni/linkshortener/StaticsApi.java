package com.github.theborakompanioni.linkshortener;

import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
class StaticsApi {

    private final Vertx vertx;

    @Autowired
    public StaticsApi(Vertx vertx) {
        this.vertx = vertx;
    }

    Router router() {
        Router router = Router.router(vertx);

        String webroot = getRoot();
        log.info("Using '{}' as webroot", webroot);

        router.route().handler(StaticHandler.create(webroot));

        return router;
    }

    private String getRoot() {
        final String pathWhenInsideJarFile = "BOOT-INF/classes/webroot";
        boolean insideJarFile = new ClassPathResource(pathWhenInsideJarFile).exists();
        return insideJarFile ? pathWhenInsideJarFile : "webroot";
    }
}
