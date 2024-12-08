package com.example.config

import zio.*
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.Duration
import scala.jdk.DurationConverters.*

/** Configuration for the Gemini API client.
  *
  * @param apiKey
  *   API key for authentication
  * @param model
  *   Name of the model to use (e.g., "gemini-pro")
  * @param temperature
  *   Controls randomness in responses (0.0 to 1.0)
  * @param maxTokens
  *   Maximum number of tokens in responses
  * @param retryConfig
  *   Configuration for retry behavior
  * @param clientTimeout
  *   HTTP client timeout duration
  */
case class GeminiConfig(
    apiKey: String,
    model: String,
    temperature: Double,
    maxTokens: Int,
    retryConfig: RetryConfig,
    clientTimeout: Duration
)

/** Configuration for retry behavior.
  *
  * @param maxAttempts
  *   Maximum number of retry attempts
  * @param initialDelay
  *   Initial delay before first retry
  * @param maxDelay
  *   Maximum delay between retries
  * @param backoffFactor
  *   Multiplier for exponential backoff
  */
case class RetryConfig(
    maxAttempts: Int,
    initialDelay: Duration,
    maxDelay: Duration,
    backoffFactor: Double
)

/** Companion object for GeminiConfig providing configuration loading.
  */
object GeminiConfig:
  /** Loads configuration from application.conf and environment variables.
    *
    * The API key can be provided through:
    * 1. Environment variable: GEMINI_API_KEY
    * 2. System property: gemini.api-key
    * 3. Configuration file: gemini.api-key
    *
    * @return
    *   A ZIO effect that loads the configuration
    */
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

  /** ZLayer that provides the GeminiConfig.
    */
  val layer: ZLayer[Any, String, GeminiConfig] = ZLayer.fromZIO(loadConfig)
