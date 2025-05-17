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


package com.io7m.zugorum.cmdline.internal;

import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QStringType;
import com.io7m.quarrel.ext.logback.QLogback;
import com.io7m.zugorum.server.ZuConfiguration;
import com.io7m.zugorum.server.ZuServer;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Run the server.
 */

public final class ZuCmdServer extends ZuCmd
{
  private static final QParameterNamed1<Path> CONFIGURATION =
    new QParameterNamed1<>(
      "--configuration",
      List.of(),
      new QStringType.QConstant("The configuration file."),
      Optional.empty(),
      Path.class
    );

  /**
   * Run the server.
   */

  public ZuCmdServer()
  {
    super(new QCommandMetadata(
      "server",
      new QStringType.QConstant("Run the server."),
      Optional.empty()
    ));
  }

  @Override
  protected QCommandStatus onExecuteActual(
    final QCommandContextType context)
    throws Exception
  {
    QLogback.configure(context);

    final var configuration =
      ZuConfiguration.ofFile(context.parameterValue(CONFIGURATION));

    try (final var server = ZuServer.create(configuration)) {
      server.start();

      while (true) {
        Thread.sleep(1_000L);
      }
    }
  }

  @Override
  protected List<QParameterNamedType<?>> onListNamedParametersActual()
  {
    return List.of(CONFIGURATION);
  }
}
