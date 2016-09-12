package com.github.theborakompanioni.linkshortener;

import com.github.theborakompanioni.linkshortener.links.LinkShortener;
import com.google.common.base.Charsets;
import com.google.common.net.HttpHeaders;
import com.google.common.primitives.Longs;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.Base64;
import java.util.Optional;

@Slf4j
class LinkShortenerApi {

    private final Vertx vertx;
    private final LinkShortener linkShortener;

    @Autowired
    public LinkShortenerApi(Vertx vertx, LinkShortener redisLinkShortener) {
        this.vertx = vertx;
        this.linkShortener = redisLinkShortener;
    }

    Router router() throws Exception {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.route("/shorten").handler(ctx -> {
            final String originalUrl = Optional.ofNullable(ctx.request().params().get("url"))
                    .map(URI::create)
                    .map(URI::toString)
                    .orElse("https://www.zombo.com");

            long id = linkShortener.put(originalUrl).toBlocking().single();

            ctx.response().end(new JsonObject()
                    .put("id", id)
                    .put("long_url", originalUrl)
                    .put("short_url", ctx.request().host() + ctx.mountPoint() + "/" + LinkIdCodec.encode(id))
                    .put("info_url", ctx.request().host() + ctx.mountPoint() + "/info/" + LinkIdCodec.encode(id))
                    .encodePrettily());
        });
        router.route("/info/:id").handler(ctx -> {
            final String idParam = ctx.pathParam("id");
            final long id = LinkIdCodec.decode(idParam)
                    .orElse(0L);

            final String url = linkShortener.get(id)
                    .toBlocking()
                    .single()
                    .orElse("https://www.zombo.com");

            ctx.response().setStatusCode(HttpResponseStatus.NOT_IMPLEMENTED.code());
            ctx.response().end(new JsonObject()
                    .put("id", id)
                    .put("short_url", ctx.request().host() + ctx.mountPoint() + "/" + LinkIdCodec.encode(id))
                    .put("long_url", url)
                    .encodePrettily());
        });

        router.route("/:id").handler(ctx -> {
            final String idParam = ctx.pathParam("id");
            final long id = LinkIdCodec.decode(idParam)
                    .orElse(0L);

            final String url = linkShortener.get(id)
                    .toBlocking()
                    .single()
                    .orElse("https://www.zombo.com");

            ctx.response().putHeader(HttpHeaders.LOCATION, url);
            ctx.response().setStatusCode(HttpResponseStatus.FOUND.code());
            ctx.response().end();
        });

        return router;
    }

    static class LinkIdCodec {
        static Optional<Long> decode(String param) {
            try {
                return Optional.ofNullable(param)
                        .map(val -> Base64.getUrlDecoder().decode(val))
                        .map(String::new)
                        .map(Longs::tryParse);
            } catch (IllegalArgumentException e) {
                log.debug("{} cannot be decoded to long: {}", param, e.getMessage());
                return Optional.empty();
            }
        }

        static String encode(long id) {
            return Base64.getUrlEncoder().encodeToString(String.valueOf(id).getBytes(Charsets.UTF_8));
        }
    }
}
