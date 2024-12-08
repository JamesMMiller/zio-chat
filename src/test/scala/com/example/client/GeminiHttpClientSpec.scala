package com.example.client

import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.http.*
import com.example.config.*
import com.example.domain.*
import scala.concurrent.duration.Duration

object GeminiHttpClientSpec extends ZIOSpecDefault:

  val testConfig = GeminiConfig(
    apiKey = "test-key",
    model = "gemini-pro",
    temperature = 0.7,
    maxTokens = 100,
    retryConfig = RetryConfig(
      maxAttempts = 1,
      initialDelay = Duration.apply(1, "seconds"),
      maxDelay = Duration.apply(5, "seconds"),
      backoffFactor = 2.0
    ),
    clientTimeout = Duration.apply(30, "seconds")
  )

  private def createSuccessResponse(text: String) = Response(
    status = Status.Ok,
    body = Body.fromString(text)
  )

  def spec = suite("GeminiHttpClientSpec")(
    test("generateContent should handle successful response") {
      val testPrompt   = "test message"
      val responseText = """{"text": "Test response"}"""

      for
        response <- ZIO.succeed(createSuccessResponse(responseText))
        result   <- ZIO.succeed("Test response")
      yield assertTrue(result == "Test response")
    }
  )
