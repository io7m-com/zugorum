zugorum
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.zugorum/com.io7m.zugorum.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.zugorum%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.zugorum/com.io7m.zugorum?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/zugorum/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/zugorum.svg?style=flat-square)](https://codecov.io/gh/io7m-com/zugorum)
![Java Version](https://img.shields.io/badge/21-java?label=java&color=e6c35c)

![com.io7m.zugorum](./src/site/resources/zugorum.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/zugorum/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/zugorum/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/zugorum/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/zugorum/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/zugorum/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/zugorum/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/zugorum/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/zugorum/actions?query=workflow%3Amain.windows.temurin.lts)|

## zugorum

The `zugorum` package provides a server for making requests to external sites
in order to test liveness, and publishing the results as [Prometheus](https://prometheus.io/)
metrics.

## Features

* Check the liveness of external sites and publish Prometheus metrics.
* A small, easily auditable codebase.
* Platform independence. No platform-dependent code is included in any form,
  and installations can largely be carried between platforms without changes.
* [OCI](https://opencontainers.org/)-ready: Ready to run as an immutable,
  stateless, read-only, unprivileged container for maximum security and
  reliability.
* [OSGi](https://www.osgi.org/)-ready.
* [JPMS](https://en.wikipedia.org/wiki/Java_Platform_Module_System)-ready.
* ISC license.

## Usage

```
{
  "ListenAddress": "0.0.0.0",
  "ListenPort": 8190,
  "Checks": [
    {
      "Type": "HTTP2xx",
      "URI": "https://example.invalid",
      "PauseMinimum": "PT1S",
      "PauseMaximum": "PT10S"
    },
    {
      "Type": "HTTP2xx",
      "URI": "https://www.io7m.com",
      "PauseMinimum": "PT1S",
      "PauseMaximum": "PT10S"
    },
    {
      "Type": "HTTP2xx",
      "URI": "http://www.io7m.com",
      "PauseMinimum": "PT1S",
      "PauseMaximum": "PT10S"
    },
    ...
}
```

```
$ zygorum server --configuration config.json
```

Prometheus metrics will be available on port `8190`.


