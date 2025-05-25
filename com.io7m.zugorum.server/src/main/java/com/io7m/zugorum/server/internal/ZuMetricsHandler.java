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

import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The metrics handler.
 */

public final class ZuMetricsHandler implements Handler
{
  private final ZuMetrics metrics;

  /**
   * The metrics handler.
   *
   * @param inMetrics The metrics store
   */

  public ZuMetricsHandler(
    final ZuMetrics inMetrics)
  {
    this.metrics =
      Objects.requireNonNull(inMetrics, "metrics");
  }

  private static final String HTTP_METRIC_TEXT = """
    # HELP zu_http_status The most recent result of an HTTP/HTTPS status check
    # TYPE zu_http_status gauge
    """.trim();

  private static final String SMTP_METRIC_TEXT = """
    # HELP zu_smtp_status The most recent result of an SMTP status check
    # TYPE zu_smtp_status gauge
    """.trim();


  private static final String TLS_METRIC_TEXT = """
    # HELP zu_tls_status The most recent result of a TLS status check
    # TYPE zu_tls_status gauge
    """.trim();

  @Override
  public void handle(
    final ServerRequest serverRequest,
    final ServerResponse serverResponse)
    throws Exception
  {
    serverResponse.header("Content-Type", "text/plain");

    try (final var output = serverResponse.outputStream()) {
      try (final var writer =
             new BufferedWriter(new OutputStreamWriter(output, UTF_8))) {
        this.writeMetrics(writer);
      }
    }
  }

  private void writeMetrics(
    final BufferedWriter writer)
    throws IOException
  {
    final var metricMap = this.metrics.metrics();
    for (final var metric : metricMap.entrySet()) {
      switch (metric.getValue()) {
        case final ZuMetrics.HTTPMetricStatus httpMetricStatus -> {
          writeMetricHTTP(writer, httpMetricStatus);
        }
        case final ZuMetrics.SMTPMetricStatus smtpMetricStatus -> {
          writeMetricSMTP(writer, smtpMetricStatus);
        }
        case final ZuMetrics.TLSMetricStatus tlsMetricStatus -> {
          writeMetricTLS(writer, tlsMetricStatus);
        }
      }
    }
  }

  private static void writeMetricTLS(
    final BufferedWriter writer,
    final ZuMetrics.TLSMetricStatus tlsMetricStatus)
    throws IOException
  {
    writer.write(TLS_METRIC_TEXT);
    writer.write("\n");

    final var metricText =
      "zu_tls_status{url=\"%s\",message=\"%s\"} %d"
        .formatted(
          tlsMetricStatus.uri(),
          tlsMetricStatus.message(),
          tlsMetricStatus.failure() ? 1 : 0
        );

    writer.append(metricText);
    writer.append("\n");
  }

  private static void writeMetricSMTP(
    final BufferedWriter writer,
    final ZuMetrics.SMTPMetricStatus smtpMetricStatus)
    throws IOException
  {
    writer.write(SMTP_METRIC_TEXT);
    writer.write("\n");

    final var metricText =
      "zu_smtp_status{url=\"%s\",message=\"%s\"} %d"
        .formatted(
          smtpMetricStatus.uri(),
          smtpMetricStatus.message(),
          smtpMetricStatus.failure() ? 1 : 0
        );

    writer.append(metricText);
    writer.append("\n");
  }

  private static void writeMetricHTTP(
    final BufferedWriter writer,
    final ZuMetrics.HTTPMetricStatus httpMetricStatus)
    throws IOException
  {
    writer.write(HTTP_METRIC_TEXT);
    writer.write("\n");

    final var messageOpt =
      httpMetricStatus.failureMessage();

    final String metricText;
    metricText = messageOpt.map(message -> {
      return "zu_http_status{url=\"%s\",message=\"%s\"} %d"
        .formatted(
          httpMetricStatus.uri(),
          message,
          httpMetricStatus.code()
        );
    }).orElseGet(() -> {
      return "zu_http_status{url=\"%s\"} %d"
        .formatted(
          httpMetricStatus.uri(),
          httpMetricStatus.code()
        );
    });

    writer.append(metricText);
    writer.append("\n");
  }
}
