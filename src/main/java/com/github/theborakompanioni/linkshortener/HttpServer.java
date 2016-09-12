package com.github.theborakompanioni.linkshortener;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
class HttpServer extends AbstractVerticle {

    private final AppConfiguration configuration;
    private final StaticsApi staticsApi;
    private final LinkShortenerApi linkShortenerApi;

    @Autowired
    public HttpServer(AppConfiguration configuration,
                      StaticsApi staticsApi, LinkShortenerApi linkShortenerApi) {
        this.configuration = configuration;
        this.staticsApi = staticsApi;
        this.linkShortenerApi = linkShortenerApi;
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router.mountSubRouter("/link", linkShortenerApi.router());
        router.mountSubRouter("/", staticsApi.router());

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(configuration.httpPort());
    }

}
