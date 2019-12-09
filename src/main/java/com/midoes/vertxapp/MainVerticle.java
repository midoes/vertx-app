package com.midoes.vertxapp;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

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
    router.get("/api*").handler(this::defaultProcessorForAllAPI);
    router.get("/api/v1/test").handler(this::getTest);
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

  // Call for all default API HTTP GET, POST, PUT and DELETE
  private void defaultProcessorForAllAPI(RoutingContext routingContext) {
    String authToken = routingContext.request().headers().get("AuthToken");
    if (authToken == null || !authToken.equals("123")) {
      routingContext.response()
        .setStatusCode(401)
        .putHeader(HttpHeaders.CONTENT_TYPE, "content-type")
        .end(Json.encodePrettily(new JsonObject().put("error", "Not Authorized to use this API")));
      LOGGER.info("Failed basic auth check");
    } else {
      LOGGER.info("Passed basic auth check");
      // Call the next matching route
      routingContext.response()
        .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
      routingContext.response()
        .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE");
      routingContext.next();
    }
  }

  private void getTest(RoutingContext routingContext) {
    JsonObject response = new JsonObject();
    response.put("test", "Test method is working!");
    routingContext.response()
      .setStatusCode(200)
      .putHeader("content-type", "application/json")
      .end(Json.encodePrettily(response));
  }

  @Override
  public void stop() {
    LOGGER.info("Hello World API Verticle App stopped");
  }

}
