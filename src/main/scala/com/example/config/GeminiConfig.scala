package com.example.config

import zio.*
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.Duration
import scala.jdk.DurationConverters.*

case class GeminiConfig(
    apiKey: String,
    model: String,
    temperature: Double,
    maxTokens: Int,
    retryConfig: RetryConfig,
    clientTimeout: Duration
)

case class RetryConfig(
    maxAttempts: Int,
    initialDelay: Duration,
    maxDelay: Duration,
    backoffFactor: Double
)

object GeminiConfig:
  private def loadConfig: IO[String, GeminiConfig] =
    ZIO.attempt {
      val config = ConfigFactory.load().getConfig("gemini")
      val apiKey = Option(config.getString("api-key"))
        .filter(_.nonEmpty)
        .getOrElse(throw new RuntimeException(
          """
          |Missing Gemini API key! Please set it using one of:
          |1. Environment variable: export GEMINI_API_KEY=your-key-here
          |2. System property: -Dgemini.api-key=your-key-here
          |3. Configuration file: gemini.api-key=your-key-here
          |
          |You can get an API key from: https://makersuite.google.com/app/apikey
          |""".stripMargin))

      val retryConfig = RetryConfig(
        maxAttempts = config.getInt("client.retry.max-attempts"),
        initialDelay = config.getDuration("client.retry.initial-delay").toScala,
        maxDelay = config.getDuration("client.retry.max-delay").toScala,
        backoffFactor = config.getDouble("client.retry.backoff-factor")
      )

      GeminiConfig(
        apiKey = apiKey,
        model = config.getString("model"),
        temperature = config.getDouble("temperature"),
        maxTokens = config.getInt("max-tokens"),
        retryConfig = retryConfig,
        clientTimeout = config.getDuration("client.timeout").toScala
      )
    }.mapError(e => e.getMessage)

  val layer: ZLayer[Any, String, GeminiConfig] = ZLayer.fromZIO(loadConfig)
