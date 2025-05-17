/*
 * Copyright © 2025 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package com.io7m.zugorum.server.internal;

import com.io7m.zugorum.server.ZuConfiguration;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;

import static java.net.StandardSocketOptions.SO_REUSEADDR;
import static java.net.StandardSocketOptions.SO_REUSEPORT;

/**
 * The metrics server.
 */

public final class ZuMetricsServer
  implements AutoCloseable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ZuMetricsServer.class);

  private final WebServer webServer;

  private ZuMetricsServer(
    final WebServer inWebServer)
  {
    this.webServer =
      Objects.requireNonNull(inWebServer, "webServer");
  }

  /**
   * Create a new metrics server.
   *
   * @param configuration The configuration
   * @param metrics       The metrics store
   *
   * @return A server
   *
   * @throws IOException On errors
   */

  public static ZuMetricsServer create(
    final ZuConfiguration configuration,
    final ZuMetrics metrics)
    throws IOException
  {
    final var routingBuilder = HttpRouting.builder();
    routingBuilder.get(
      "/",
      new ZuMetricsHandler(metrics)
    );

    final var webServerBuilder =
      WebServerConfig.builder();

    final var address =
      InetAddress.getByName(configuration.listenAddress());
    final var port =
      configuration.listenPort();

    final var webServer =
      webServerBuilder
        .port(port)
        .address(address)
        .listenerSocketOptions(Map.ofEntries(
          Map.entry(SO_REUSEADDR, Boolean.TRUE),
          Map.entry(SO_REUSEPORT, Boolean.TRUE)
        ))
        .routing(routingBuilder)
        .build();

    webServer.start();
    LOG.info("[{}:{}] Metrics server started", address, port);
    return new ZuMetricsServer(webServer);
  }

  @Override
  public void close()
  {
    this.webServer.stop();
  }
}
