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

import org.slf4j.Logger;

import java.time.Duration;
import java.util.Objects;

/**
 * A convenient abstract base class for checks.
 */

public abstract class ZuCheckAbstract
  implements ZuCheckType
{
  private final Logger logger;
  private final ZuMetrics metrics;

  protected ZuCheckAbstract(
    final Logger inLogger,
    final ZuMetrics inMetrics)
  {
    this.logger =
      Objects.requireNonNull(inLogger, "logger");
    this.metrics =
      Objects.requireNonNull(inMetrics, "metrics");
  }

  protected final ZuMetrics metrics()
  {
    return this.metrics;
  }

  protected final void pauseRandom(
    final Duration min,
    final Duration max)
  {
    try {
      final var duration = ZuPauses.randomPauseOf(min, max);
      this.logger.debug("Pausing for {}", duration);
      Thread.sleep(duration);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
