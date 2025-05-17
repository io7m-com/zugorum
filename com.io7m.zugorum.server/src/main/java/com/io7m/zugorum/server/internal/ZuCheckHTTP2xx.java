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
import com.io7m.zugorum.server.ZuVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

final class ZuCheckHTTP2xx
  extends ZuCheckAbstract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ZuCheckHTTP2xx.class);

  private final ZuConfiguration.CheckHTTP2xx config;
  private final HttpClient client;

  ZuCheckHTTP2xx(
    final ZuMetrics m,
    final ZuConfiguration.CheckHTTP2xx inConfig)
  {
    super(LOG, m);

    this.config =
      Objects.requireNonNull(inConfig, "config");
    this.client =
      HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build();
  }

  @Override
  public void run()
  {
    MDC.put("URI", this.config.uri().toString());
    MDC.put("Type", this.config.type());
    LOG.info("Check started.");

    final var metrics = this.metrics();
    metrics.httpStatus(this.config.uri(), 0);

    while (true) {
      this.pauseRandom(this.config.pauseMinimum(), this.config.pauseMaximum());
      this.makeRequest();
    }
  }

  private void makeRequest()
  {
    final var metrics =
      this.metrics();

    try {
      MDC.put("URI", this.config.uri().toString());
      MDC.put("Type", this.config.type());
      LOG.debug("Sending request.");

      final var request =
        HttpRequest.newBuilder(this.config.uri())
          .header("User-Agent", userAgent())
          .GET()
          .build();

      final var response =
        this.client.send(request, HttpResponse.BodyHandlers.discarding());

      MDC.put("Status", Integer.toString(response.statusCode()));
      if (response.statusCode() >= 400) {
        LOG.error("Request received an error.");
      } else {
        LOG.info("Request succeeded.");
      }
      MDC.remove("Status");

      metrics.httpStatus(this.config.uri(), response.statusCode());
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (final Exception e) {
      LOG.error("Request exception: ", e);
      metrics.httpException(this.config.uri(), e);
    }
  }

  private static String userAgent()
  {
    return "com.io7m.zugorum %s %s".formatted(
      ZuVersion.MAIN_VERSION,
      ZuVersion.MAIN_BUILD
    );
  }
}
