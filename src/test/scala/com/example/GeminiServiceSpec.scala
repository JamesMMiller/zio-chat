package com.example

import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.json.*
import com.example.config.{GeminiConfig, RetryConfig}
import scala.concurrent.duration.Duration
import com.example.domain.*
import com.example.domain.GeminiProtocol.given
import com.example.client.GeminiClient
import com.example.service.*

object GeminiServiceSpec extends ZIOSpecDefault:
  // Test fixtures
  private val testConfig = GeminiConfig(
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

  private def createMockClient(response: IO[GeminiError, GeminiResponse]) = new GeminiClient:
    def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] = response

  private def createSuccessResponse(text: String) = GeminiResponse(
    candidates = List(
      GeminiCandidate(
        content = GeminiContent(
          parts = List(GeminiPart(text)),
          role = "model"
        )
      )
    ),
    promptFeedback = None
  )

  def spec = suite("GeminiServiceSpec")(
    test("generateContent should return response from client") {
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

      val testPrompt = "test message"
      val expectedResponse = "Test response"
      val mockClient = createMockClient(ZIO.succeed(createSuccessResponse(expectedResponse)))

      for
        result <- ZIO.serviceWithZIO[GeminiService](_.generateContent(testPrompt))
          .provide(
            ZLayer.succeed(testConfig),
            ZLayer.succeed(mockClient),
            ConversationManager.layer,
            GeminiService.layer
          )
      yield assertTrue(result == expectedResponse)
    },

    test("should propagate API errors") {
      val testPrompt = "test message"
      val expectedError = ApiError("Test error")
      val mockClient = createMockClient(ZIO.fail(expectedError))

      for
        result <- ZIO.serviceWithZIO[GeminiService](_.generateContent(testPrompt))
          .provide(
            ZLayer.succeed(testConfig),
            ZLayer.succeed(mockClient),
            ConversationManager.layer,
            GeminiService.layer
          )
          .exit
      yield assert(result)(fails(equalTo(expectedError)))
    },

    test("should handle content blocking") {
      val testPrompt = "test message"
      val blockReason = "Content blocked"
      val blockedResponse = GeminiResponse(
        candidates = List(
          GeminiCandidate(
            content = GeminiContent(
              parts = List(GeminiPart("Blocked content")),
              role = "model"
            )
          )
        ),
        promptFeedback = Some(PromptFeedback(Some(blockReason)))
      )
      val mockClient = createMockClient(ZIO.succeed(blockedResponse))

      for
        result <- ZIO.serviceWithZIO[GeminiService](_.generateContent(testPrompt))
          .provide(
            ZLayer.succeed(testConfig),
            ZLayer.succeed(mockClient),
            ConversationManager.layer,
            GeminiService.layer
          )
          .exit
      yield assert(result)(fails(equalTo(ApiError(s"Content blocked: $blockReason"))))
    },

    suite("conversation history")(
      test("should maintain conversation history") {
        for
          mockClient <- ZIO.succeed(createMockClient(ZIO.succeed(createSuccessResponse("Response"))))
          service <- ZIO.service[GeminiService].provide(
            ZLayer.succeed(testConfig),
            ZLayer.succeed(mockClient),
            ConversationManager.layer,
            GeminiService.layer
          )
          _      <- service.generateContent("First message")
          _      <- service.generateContent("Second message")
          result <- service.generateContent("Third message")
        yield assertTrue(result == "Response")
      },

      test("should clear history when requested") {
        for
          mockClient <- ZIO.succeed(createMockClient(ZIO.succeed(createSuccessResponse("Response"))))
          service <- ZIO.service[GeminiService].provide(
            ZLayer.succeed(testConfig),
            ZLayer.succeed(mockClient),
            ConversationManager.layer,
            GeminiService.layer
          )
          _      <- service.generateContent("Initial message")
          _      <- service.clearHistory
          result <- service.generateContent("New message")
        yield assertTrue(result == "Response")
      }
    )
  )