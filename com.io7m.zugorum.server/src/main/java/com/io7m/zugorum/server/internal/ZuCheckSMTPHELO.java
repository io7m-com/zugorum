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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Objects;

final class ZuCheckSMTPHELO
  extends ZuCheckAbstract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ZuCheckSMTPHELO.class);

  private final ZuConfiguration.CheckSMTPHELO config;

  ZuCheckSMTPHELO(
    final ZuMetrics m,
    final ZuConfiguration.CheckSMTPHELO inConfig)
  {
    super(LOG, m);

    this.config =
      Objects.requireNonNull(inConfig, "config");
  }

  @Override
  public void run()
  {
    MDC.put("URI", this.config.uri().toString());
    MDC.put("Type", this.config.type());
    LOG.info("Check started.");

    final var metrics = this.metrics();
    metrics.smtpOK(this.config.uri(), "");

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
      var port =
        this.config.uri().getPort();

      if (port == -1) {
        port = 25;
      }

      try (final var socket = new Socket(host, port)) {
        final var out =
          new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        final var in =
          new BufferedReader(new InputStreamReader(socket.getInputStream()));

        {
          in.readLine();
        }

        out.write("EHLO %s\n".formatted(this.config.helo()));
        out.write("\n");
        out.flush();

        {
          final var r = in.readLine();
          if (!r.startsWith("250")) {
            LOG.error("Request failed: {}", r);
            metrics.smtpError(this.config.uri(), r);
            return;
          }
        }

        out.write("QUIT\n");
        out.flush();
      }

      LOG.info("Request succeeded.");
      metrics.smtpOK(this.config.uri(), "");
    } catch (final Exception e) {
      LOG.error("Request exception: ", e);
      metrics.smtpException(this.config.uri(), e);
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
