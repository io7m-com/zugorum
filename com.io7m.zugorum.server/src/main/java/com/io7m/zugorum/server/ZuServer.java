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


package com.io7m.zugorum.server;

import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.jmulticlose.core.ClosingResourceFailedException;
import com.io7m.zugorum.server.internal.ZuCheckRunner;
import com.io7m.zugorum.server.internal.ZuMetrics;
import com.io7m.zugorum.server.internal.ZuMetricsServer;

import java.io.IOException;
import java.util.Objects;

/**
 * The main server.
 */

public final class ZuServer implements AutoCloseable
{
  private final CloseableCollectionType<ClosingResourceFailedException> resources;
  private final ZuConfiguration configuration;

  private ZuServer(
    final ZuConfiguration inConfiguration)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.resources =
      CloseableCollection.create();
  }

  /**
   * Create a server.
   *
   * @param configuration The configuration
   *
   * @return The server
   */

  public static ZuServer create(
    final ZuConfiguration configuration)
  {
    return new ZuServer(configuration);
  }

  /**
   * Start the server.
   *
   * @throws Exception On errors
   */

  public void start()
    throws Exception
  {
    final var metrics = new ZuMetrics();
    this.resources.add(this.createHTTPServer(metrics));
    this.resources.add(this.createCheckRunner(metrics));
  }

  private AutoCloseable createCheckRunner(
    final ZuMetrics metrics)
  {
    return ZuCheckRunner.create(this.configuration, metrics);
  }

  private AutoCloseable createHTTPServer(
    final ZuMetrics metrics)
    throws IOException
  {
    return ZuMetricsServer.create(this.configuration, metrics);
  }

  @Override
  public void close()
    throws Exception
  {
    this.resources.close();
  }
}
