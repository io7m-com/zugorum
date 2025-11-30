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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

// CHECKSTYLE:OFF

/**
 * The server configuration.
 */

@JsonSerialize
@JsonDeserialize
public record ZuConfiguration(
  @JsonProperty(value = "ListenAddress", required = true)
  @JsonPropertyDescription("The address on which the server will listen.")
  String listenAddress,

  @JsonProperty(value = "ListenPort", required = true)
  @JsonPropertyDescription("The port on which the server will listen.")
  int listenPort,

  @JsonProperty(value = "Checks", required = true)
  @JsonPropertyDescription("The list of checks.")
  List<CheckType> checks)
{
  public ZuConfiguration
  {
    Objects.requireNonNull(listenAddress, "listenAddress");
    checks = List.copyOf(checks);
  }

  public sealed interface CheckType
  {
    @JsonProperty(value = "Type", required = true)
    @JsonPropertyDescription("The check type.")
    String type();

    @JsonProperty(value = "PauseMinimum")
    @JsonPropertyDescription("The minimum pause time.")
    Duration pauseMinimum();

    @JsonProperty(value = "PauseMaximum")
    @JsonPropertyDescription("The maximum pause time.")
    Duration pauseMaximum();
  }

  public record CheckHTTP2xx(
    @JsonProperty(value = "Type", required = true)
    @JsonPropertyDescription("The check type.")
    String type,

    @JsonProperty(value = "URI", required = true)
    @JsonPropertyDescription("The target address.")
    URI uri,

    @JsonProperty(value = "PauseMinimum")
    @JsonPropertyDescription("The minimum pause time.")
    Duration pauseMinimum,

    @JsonProperty(value = "PauseMaximum")
    @JsonPropertyDescription("The maximum pause time.")
    Duration pauseMaximum)
    implements CheckType
  {
    /**
     * The check type.
     */

    public static final String TYPE =
      "HTTP2xx";

    public CheckHTTP2xx
    {
      Objects.requireNonNull(type, "type");
      Objects.requireNonNull(uri, "uri");

      pauseMinimum =
        Objects.requireNonNullElse(pauseMinimum, Duration.ofSeconds(60L));
      pauseMaximum =
        Objects.requireNonNullElse(pauseMaximum, Duration.ofMinutes(10L));

      if (pauseMaximum.compareTo(pauseMinimum) < 0) {
        pauseMaximum = pauseMinimum;
      }

      if (!type.equals(TYPE)) {
        throw new IllegalArgumentException(
          "Type must be %s".formatted(TYPE));
      }
    }
  }

  public record CheckSMTPHELO(
    @JsonProperty(value = "Type", required = true)
    @JsonPropertyDescription("The check type.")
    String type,

    @JsonProperty(value = "URI", required = true)
    @JsonPropertyDescription("The target address.")
    URI uri,

    @JsonProperty(value = "HELO", required = true)
    @JsonPropertyDescription("The HELO message.")
    String helo,

    @JsonProperty(value = "PauseMinimum")
    @JsonPropertyDescription("The minimum pause time.")
    Duration pauseMinimum,

    @JsonProperty(value = "PauseMaximum")
    @JsonPropertyDescription("The maximum pause time.")
    Duration pauseMaximum)
    implements CheckType
  {
    /**
     * The check type.
     */

    public static final String TYPE =
      "SMTPHELO";

    public CheckSMTPHELO
    {
      Objects.requireNonNull(type, "type");
      Objects.requireNonNull(uri, "uri");
      Objects.requireNonNull(helo, "helo");

      pauseMinimum =
        Objects.requireNonNullElse(pauseMinimum, Duration.ofSeconds(60L));
      pauseMaximum =
        Objects.requireNonNullElse(pauseMaximum, Duration.ofMinutes(10L));

      if (pauseMaximum.compareTo(pauseMinimum) < 0) {
        pauseMaximum = pauseMinimum;
      }

      if (!type.equals(TYPE)) {
        throw new IllegalArgumentException(
          "Type must be %s".formatted(TYPE));
      }
    }
  }

  public record CheckTLS(
    @JsonProperty(value = "Type", required = true)
    @JsonPropertyDescription("The check type.")
    String type,

    @JsonProperty(value = "URI", required = true)
    @JsonPropertyDescription("The target address.")
    URI uri,

    @JsonProperty(value = "PauseMinimum")
    @JsonPropertyDescription("The minimum pause time.")
    Duration pauseMinimum,

    @JsonProperty(value = "PauseMaximum")
    @JsonPropertyDescription("The maximum pause time.")
    Duration pauseMaximum)
    implements CheckType
  {
    /**
     * The check type.
     */

    public static final String TYPE =
      "TLS";

    public CheckTLS
    {
      Objects.requireNonNull(type, "type");
      Objects.requireNonNull(uri, "uri");

      pauseMinimum =
        Objects.requireNonNullElse(pauseMinimum, Duration.ofSeconds(60L));
      pauseMaximum =
        Objects.requireNonNullElse(pauseMaximum, Duration.ofMinutes(10L));

      if (pauseMaximum.compareTo(pauseMinimum) < 0) {
        pauseMaximum = pauseMinimum;
      }

      if (!type.equals(TYPE)) {
        throw new IllegalArgumentException(
          "Type must be %s".formatted(TYPE));
      }
    }
  }

  public static final class CheckDeserializer
    extends ValueDeserializer<CheckType>
  {
    public CheckDeserializer()
    {

    }

    @Override
    public Class<CheckType> handledType()
    {
      return CheckType.class;
    }

    @Override
    public CheckType deserialize(
      final JsonParser jsonParser,
      final DeserializationContext ctx)
    {
      final ObjectNode node =
        jsonParser.readValueAsTree();
      final var typeNode =
        node.get("Type");

      if (typeNode == null) {
        throw MismatchedInputException.from(
          jsonParser,
          "Missing Type node in check."
        );
      }

      final var type =
        typeNode.asString();

      return switch (type) {
        case CheckHTTP2xx.TYPE -> {
          yield ctx.readTreeAsValue(node, CheckHTTP2xx.class);
        }
        case CheckSMTPHELO.TYPE -> {
          yield ctx.readTreeAsValue(node, CheckSMTPHELO.class);
        }
        case CheckTLS.TYPE -> {
          yield ctx.readTreeAsValue(node, CheckTLS.class);
        }
        default -> {
          throw MismatchedInputException.from(
            jsonParser,
            "Unrecognized check type: %s".formatted(type)
          );
        }
      };
    }
  }

  public static final class ConfigurationModule
    extends JacksonModule
  {
    public ConfigurationModule()
    {

    }

    @Override
    public String getModuleName()
    {
      return "com.io7m.zugorum.server";
    }

    @Override
    public Version version()
    {
      return new Version(
        1,
        0,
        0,
        null,
        "com.io7m.zugorum.server",
        "com.io7m.zugorum.server"
      );
    }

    @Override
    public void setupModule(
      final SetupContext setupContext)
    {
      final var deserializers = new SimpleDeserializers();
      deserializers.addDeserializer(CheckType.class, new CheckDeserializer());
      setupContext.addDeserializers(deserializers);
    }
  }

  public static ZuConfiguration ofFile(
    final Path file)
    throws IOException
  {
    Objects.requireNonNull(file, "file");

    final var mapper =
      JsonMapper.builder()
        .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
        .addModule(new ConfigurationModule())
        .build();

    try (final var stream = Files.newInputStream(file)) {
      return mapper.readValue(stream, ZuConfiguration.class);
    }
  }
}
