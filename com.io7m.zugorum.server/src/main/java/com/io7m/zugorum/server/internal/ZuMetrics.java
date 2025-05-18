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

import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * The metrics store.
 */

public final class ZuMetrics
{
  private final ConcurrentSkipListMap<URI, MetricType> metrics;
  private final SortedMap<URI, MetricType> metricsRead;

  /**
   * The metrics store.
   */

  public ZuMetrics()
  {
    this.metrics =
      new ConcurrentSkipListMap<>();
    this.metricsRead =
      Collections.unmodifiableSortedMap(this.metrics);
  }

  /**
   * @return A read-only view of the metric store
   */

  public SortedMap<URI, MetricType> metrics()
  {
    return this.metricsRead;
  }

  /**
   * Report an HTTP status.
   *
   * @param uri        The URI
   * @param statusCode The status code
   */

  public void httpStatus(
    final URI uri,
    final int statusCode)
  {
    Objects.requireNonNull(uri, "uri");

    this.metrics.put(
      uri,
      new HTTPMetricStatus(uri, statusCode, Optional.empty())
    );
  }

  /**
   * Report an HTTP exception.
   *
   * @param uri       The URI
   * @param exception The exception
   */

  public void httpException(
    final URI uri,
    final Exception exception)
  {
    Objects.requireNonNull(uri, "uri");
    Objects.requireNonNull(exception, "e");

    final var message =
      Objects.requireNonNullElse(
        exception.getMessage(),
        exception.getClass().getName()
      );

    this.metrics.put(uri, new SMTPMetricStatus(uri, true, message));
  }

  /**
   * Report an HTTP error.
   *
   * @param uri     The URI
   * @param message The message
   */

  public void smtpError(
    final URI uri,
    final String message)
  {
    Objects.requireNonNull(uri, "uri");

    this.metrics.put(
      uri,
      new SMTPMetricStatus(uri, true, message)
    );
  }

  /**
   * Report an SMTP exception.
   *
   * @param uri       The URI
   * @param exception The exception
   */

  public void smtpException(
    final URI uri,
    final Exception exception)
  {
    Objects.requireNonNull(uri, "uri");
    Objects.requireNonNull(exception, "e");

    final var message =
      Objects.requireNonNullElse(
        exception.getMessage(),
        exception.getClass().getName()
      );

    this.metrics.put(uri, new SMTPMetricStatus(uri, true, message));
  }

  /**
   * Report that SMTP is OK.
   *
   * @param uri     The URI
   * @param message The message
   */

  public void smtpOK(
    final URI uri,
    final String message)
  {
    Objects.requireNonNull(uri, "uri");

    this.metrics.put(
      uri,
      new SMTPMetricStatus(uri, false, message)
    );
  }

  /**
   * Report that TLS is OK.
   *
   * @param uri The URI
   */

  public void tlsOK(
    final URI uri)
  {
    Objects.requireNonNull(uri, "uri");

    this.metrics.put(
      uri,
      new SMTPMetricStatus(uri, false, "")
    );
  }

  /**
   * Report a TLS exception.
   *
   * @param uri       The URI
   * @param exception The exception
   */

  public void tlsException(
    final URI uri,
    final Exception exception)
  {
    Objects.requireNonNull(uri, "uri");

    final var message =
      Objects.requireNonNullElse(
        exception.getMessage(),
        exception.getClass().getName()
      );

    this.metrics.put(
      uri,
      new SMTPMetricStatus(uri, false, message)
    );
  }

  /**
   * The type of recorded metrics.
   */

  public sealed interface MetricType
  {
    /**
     * @return The URI
     */

    URI uri();
  }

  /**
   * An HTTP status metric.
   *
   * @param uri            The URI
   * @param code           The status code
   * @param failureMessage The failure message, if any
   */

  public record HTTPMetricStatus(
    URI uri,
    int code,
    Optional<String> failureMessage)
    implements MetricType
  {
    /**
     * An HTTP status metric.
     *
     * @param uri            The URI
     * @param code           The status code
     * @param failureMessage The failure message, if any
     */

    public HTTPMetricStatus
    {
      Objects.requireNonNull(uri, "uri");
      Objects.requireNonNull(failureMessage, "failureMessage");
    }
  }

  /**
   * An SMTP status metric.
   *
   * @param uri     The URI
   * @param failure @{code true} if the request failed
   * @param message The message
   */

  public record SMTPMetricStatus(
    URI uri,
    boolean failure,
    String message)
    implements MetricType
  {
    /**
     * An SMTP status metric.
     *
     * @param uri     The URI
     * @param failure @{code true} if the request failed
     * @param message The message
     */

    public SMTPMetricStatus
    {
      Objects.requireNonNull(uri, "uri");
      Objects.requireNonNull(message, "message");
    }
  }

  /**
   * A TLS status metric.
   *
   * @param uri     The URI
   * @param failure @{code true} if the request failed
   * @param message The message
   */

  public record TLSMetricStatus(
    URI uri,
    boolean failure,
    String message)
    implements MetricType
  {
    /**
     * A TLS status metric.
     *
     * @param uri     The URI
     * @param failure @{code true} if the request failed
     * @param message The message
     */

    public TLSMetricStatus
    {
      Objects.requireNonNull(uri, "uri");
      Objects.requireNonNull(message, "message");
    }
  }
}
