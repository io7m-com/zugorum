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

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.util.Objects;

final class ZuCheckTLS
  extends ZuCheckAbstract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ZuCheckTLS.class);

  private final ZuConfiguration.CheckTLS config;
  private final SocketFactory sockets;

  ZuCheckTLS(
    final ZuMetrics m,
    final ZuConfiguration.CheckTLS inConfig)
  {
    super(LOG, m);

    this.config =
      Objects.requireNonNull(inConfig, "config");
    this.sockets =
      SSLSocketFactory.getDefault();
  }

  @Override
  public void run()
  {
    MDC.put("URI", this.config.uri().toString());
    MDC.put("Type", this.config.type());
    LOG.info("Check started.");

    final var metrics = this.metrics();
    metrics.tlsOK(this.config.uri());

    while (true) {
      this.makeRequest();
      this.pauseRandom(this.config.pauseMinimum(), this.config.pauseMaximum());
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

      final var host =
        this.config.uri().getHost();
      final var port =
        this.config.uri().getPort();

      if (port == -1) {
        throw new IllegalArgumentException("Missing port in URI!");
      }

      try (final var ignored = this.sockets.createSocket(host, port)) {
        LOG.info("Request succeeded.");
      }

      metrics.tlsOK(this.config.uri());
    } catch (final Exception e) {
      LOG.error("Request exception: ", e);
      metrics.tlsException(this.config.uri(), e);
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
