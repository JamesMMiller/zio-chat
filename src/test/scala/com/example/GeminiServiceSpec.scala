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
      val testPrompt       = "test message"
      val expectedResponse = "Test response"
      val mockClient       = createMockClient(ZIO.succeed(createSuccessResponse(expectedResponse)))

      for result <- ZIO
          .serviceWithZIO[GeminiService](_.generateContent(testPrompt))
          .provide(
            ZLayer.succeed(testConfig),
            ZLayer.succeed(mockClient),
            ConversationManager.layer,
            GeminiService.layer
          )
      yield assertTrue(result == expectedResponse)
    },
    test("should propagate API errors") {
      val testPrompt    = "test message"
      val expectedError = ApiError("Test error")
      val mockClient    = createMockClient(ZIO.fail(expectedError))

      for result <- ZIO
          .serviceWithZIO[GeminiService](_.generateContent(testPrompt))
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
      val testPrompt  = "test message"
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

      for result <- ZIO
          .serviceWithZIO[GeminiService](_.generateContent(testPrompt))
          .provide(
            ZLayer.succeed(testConfig),
            ZLayer.succeed(mockClient),
            ConversationManager.layer,
            GeminiService.layer
          )
          .exit
      yield assert(result)(fails(equalTo(ApiError(s"Content blocked: $blockReason"))))
    },
    test("should handle empty response candidates") {
      val testPrompt = "test message"
      val emptyResponse = GeminiResponse(
        candidates = List.empty,
        promptFeedback = None
      )
      val mockClient = createMockClient(ZIO.succeed(emptyResponse))

      for result <- ZIO
          .serviceWithZIO[GeminiService](_.generateContent(testPrompt))
          .provide(
            ZLayer.succeed(testConfig),
            ZLayer.succeed(mockClient),
            ConversationManager.layer,
            GeminiService.layer
          )
          .exit
      yield assert(result)(fails(equalTo(ApiError("Empty response from model"))))
    },
    test("should handle network errors") {
      val testPrompt = "test message"
      val networkError = NetworkError(new java.net.ConnectException("Connection failed"))
      val mockClient = createMockClient(ZIO.fail(networkError))

      for result <- ZIO
          .serviceWithZIO[GeminiService](_.generateContent(testPrompt))
          .provide(
            ZLayer.succeed(testConfig),
            ZLayer.succeed(mockClient),
            ConversationManager.layer,
            GeminiService.layer
          )
          .exit
      yield assert(result)(fails(equalTo(networkError)))
    },
    test("should respect temperature configuration") {
      val testPrompt = "test message"
      val customTemp = 0.9
      val customConfig = testConfig.copy(temperature = customTemp)
      var capturedRequest: Option[GeminiRequest] = None
      
      val mockClient = new GeminiClient:
        def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] =
          ZIO.succeed {
            capturedRequest = Some(request)
            createSuccessResponse("Test response")
          }

      for
        _ <- ZIO
          .serviceWithZIO[GeminiService](_.generateContent(testPrompt))
          .provide(
            ZLayer.succeed(customConfig),
            ZLayer.succeed(mockClient),
            ConversationManager.layer,
            GeminiService.layer
          )
      yield assertTrue(
        capturedRequest.exists(_.generationConfig.exists(_.temperature.contains(customTemp)))
      )
    },
    test("should respect max tokens configuration") {
      val testPrompt = "test message"
      val customTokens = 200
      val customConfig = testConfig.copy(maxTokens = customTokens)
      var capturedRequest: Option[GeminiRequest] = None
      
      val mockClient = new GeminiClient:
        def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] =
          ZIO.succeed {
            capturedRequest = Some(request)
            createSuccessResponse("Test response")
          }

      for
        _ <- ZIO
          .serviceWithZIO[GeminiService](_.generateContent(testPrompt))
          .provide(
            ZLayer.succeed(customConfig),
            ZLayer.succeed(mockClient),
            ConversationManager.layer,
            GeminiService.layer
          )
      yield assertTrue(
        capturedRequest.exists(_.generationConfig.exists(_.maxOutputTokens.contains(customTokens)))
      )
    },
    suite("conversation history")(
      test("should maintain conversation history") {
        val messages = List("First", "Second", "Third")
        var capturedRequests = List.empty[GeminiRequest]
        
        val mockClient = new GeminiClient:
          def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] =
            ZIO.succeed {
              capturedRequests = request :: capturedRequests
              createSuccessResponse("Response")
            }

        for
          service <- ZIO
            .service[GeminiService]
            .provide(
              ZLayer.succeed(testConfig),
              ZLayer.succeed(mockClient),
              ConversationManager.layer,
              GeminiService.layer
            )
          _      <- ZIO.foreach(messages)(service.generateContent)
        yield assertTrue(
          // First request should have 1 message
          capturedRequests(2).contents.length == 1 &&
          capturedRequests(2).contents.head.parts.head.text == "First" &&
          // Second request should have 3 messages (user, model, user)
          capturedRequests(1).contents.length == 3 &&
          capturedRequests(1).contents.map(_.role) == List("user", "model", "user") &&
          capturedRequests(1).contents.last.parts.head.text == "Second" &&
          // Third request should have 5 messages (user, model, user, model, user)
          capturedRequests.head.contents.length == 5 &&
          capturedRequests.head.contents.map(_.role) == List("user", "model", "user", "model", "user") &&
          capturedRequests.head.contents.last.parts.head.text == "Third"
        )
      },
      test("should clear history when requested") {
        var capturedRequests = List.empty[GeminiRequest]
        
        val mockClient = new GeminiClient:
          def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] =
            ZIO.succeed {
              capturedRequests = request :: capturedRequests
              createSuccessResponse("Response")
            }

        for
          service <- ZIO
            .service[GeminiService]
            .provide(
              ZLayer.succeed(testConfig),
              ZLayer.succeed(mockClient),
              ConversationManager.layer,
              GeminiService.layer
            )
          _      <- service.generateContent("Initial message")
          _      <- service.clearHistory
          _      <- service.generateContent("New message")
        yield assertTrue(
          capturedRequests.head.contents.length == 1,
          capturedRequests.head.contents.head.parts.head.text == "New message"
        )
      },
      test("should alternate user and model messages") {
        var capturedRequest: Option[GeminiRequest] = None
        
        val mockClient = new GeminiClient:
          def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] =
            ZIO.succeed {
              capturedRequest = Some(request)
              createSuccessResponse("Response")
            }

        for
          service <- ZIO
            .service[GeminiService]
            .provide(
              ZLayer.succeed(testConfig),
              ZLayer.succeed(mockClient),
              ConversationManager.layer,
              GeminiService.layer
            )
          _      <- service.generateContent("First")
          _      <- service.generateContent("Second")
        yield assertTrue(
          capturedRequest.exists(req =>
            req.contents.length == 3 &&
            req.contents.map(_.role) == List("user", "model", "user")
          )
        )
      }
    )
  )
