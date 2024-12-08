package com.example.config

import com.typesafe.config.{Config, ConfigFactory}
import zio.*
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.jdk.DurationConverters.*

case class RetryConfig(
    maxAttempts: Int,
    initialDelay: Duration,
    maxDelay: Duration,
    backoffFactor: Double
)

case class ClientConfig(
    retry: RetryConfig,
    timeout: Duration
)

case class GeminiConfig(
    apiKey: String,
    model: String,
    temperature: Double,
    maxTokens: Int,
    client: ClientConfig
)

object GeminiConfig:

  def load: Task[GeminiConfig] = ZIO.attempt {
    val config = ConfigFactory.load().getConfig("gemini")

    val retryConfig =
      val c = config.getConfig("client.retry")
      RetryConfig(
        maxAttempts = c.getInt("max-attempts"),
        initialDelay = c.getDuration("initial-delay").toScala,
        maxDelay = c.getDuration("max-delay").toScala,
        backoffFactor = c.getDouble("backoff-factor")
      )

    val clientConfig = ClientConfig(
      retry = retryConfig,
      timeout = config.getDuration("client.timeout").toScala
    )

    GeminiConfig(
      apiKey = config.getString("api-key"),
      model = config.getString("model"),
      temperature = config.getDouble("temperature"),
      maxTokens = config.getInt("max-tokens"),
      client = clientConfig
    )
  }

  val layer: ZLayer[Any, Throwable, GeminiConfig] = ZLayer.fromZIO(load)
