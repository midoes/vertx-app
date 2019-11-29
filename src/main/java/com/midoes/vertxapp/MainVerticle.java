package com.midoes.vertxapp;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
    configRetriever.getConfig(config -> {
      if (config.succeeded()) {
        JsonObject configJson = config.result();
        DeploymentOptions options = new DeploymentOptions().setConfig(configJson);
        vertx.deployVerticle(new MainVerticle(), options);
      }
    });
  }

  @Override
  public void start() {
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    router.route().handler(routingContext -> {
      // This handler will be called for every request
      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "text/plain");
      // Write to the response and end it
      response.end("Hello World from Vert.x-Web!");
    });
    server.requestHandler(router).listen(config().getInteger("http.port"));
    LOGGER.info("Hello World API Verticle App started");
  }

  @Override
  public void stop() {
    LOGGER.info("Hello World API Verticle App stopped");
  }

}
