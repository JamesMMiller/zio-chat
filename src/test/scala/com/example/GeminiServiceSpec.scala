package com.example

import zio.*
import zio.test.*
import zio.test.Assertion.*
import com.example.service.*
import com.example.domain.*
import com.example.client.GeminiClient
import com.example.config.*
import scala.concurrent.duration.DurationInt

object GeminiServiceSpec extends ZIOSpecDefault:

  def spec = suite("GeminiServiceSpec")(
    test("should generate content successfully") {
      for
        ref <- ZIO.service[Ref[Option[GeminiRequest]]]
        service <- ZIO.service[GeminiService]
        result  <- service.generateContent("test prompt")
        request <- ref.get
      yield assertTrue(
        result == "Generated content",
        request.isDefined,
        request.get.contents.head.parts.head.text == "test prompt"
      )
    }.provide(
      mockConfig,
      successMockClient,
      mockConversationManager,
      GeminiService.layer,
      ZLayer.succeed(successRef)
    ),

    test("should handle content blocking") {
      for
        ref <- ZIO.service[Ref[Option[GeminiRequest]]]
        service <- ZIO.service[GeminiService]
        result  <- service.generateContent("test prompt").exit
        request <- ref.get
      yield assertTrue(
        result.isFailure,
        request.isDefined,
        request.get.contents.head.parts.head.text == "test prompt"
      )
    }.provide(
      mockConfig,
      blockingMockClient,
      mockConversationManager,
      GeminiService.layer,
      ZLayer.succeed(blockingRef)
    ),

    test("should handle empty response candidates") {
      for
        ref <- ZIO.service[Ref[Option[GeminiRequest]]]
        service <- ZIO.service[GeminiService]
        result  <- service.generateContent("test prompt").exit
        request <- ref.get
      yield assertTrue(
        result.isFailure,
        request.isDefined,
        request.get.contents.head.parts.head.text == "test prompt"
      )
    }.provide(
      mockConfig,
      emptyResponseMockClient,
      mockConversationManager,
      GeminiService.layer,
      ZLayer.succeed(emptyResponseRef)
    ),

    test("should handle empty parts in response") {
      for
        ref <- ZIO.service[Ref[Option[GeminiRequest]]]
        service <- ZIO.service[GeminiService]
        result  <- service.generateContent("test prompt").exit
        request <- ref.get
      yield assertTrue(
        result.isFailure,
        request.isDefined,
        request.get.contents.head.parts.head.text == "test prompt"
      )
    }.provide(
      mockConfig,
      emptyPartsMockClient,
      mockConversationManager,
      GeminiService.layer,
      ZLayer.succeed(emptyPartsRef)
    ),

    test("should handle missing text in response") {
      for
        ref <- ZIO.service[Ref[Option[GeminiRequest]]]
        service <- ZIO.service[GeminiService]
        result  <- service.generateContent("test prompt").exit
        request <- ref.get
      yield assertTrue(
        result.isFailure,
        request.isDefined,
        request.get.contents.head.parts.head.text == "test prompt"
      )
    }.provide(
      mockConfig,
      missingTextMockClient,
      mockConversationManager,
      GeminiService.layer,
      ZLayer.succeed(missingTextRef)
    ),

    test("should handle network errors") {
      for
        ref <- ZIO.service[Ref[Option[GeminiRequest]]]
        service <- ZIO.service[GeminiService]
        result  <- service.generateContent("test prompt").exit
        request <- ref.get
      yield assertTrue(
        result.isFailure,
        request.isDefined,
        request.get.contents.head.parts.head.text == "test prompt"
      )
    }.provide(
      mockConfig,
      networkErrorMockClient,
      mockConversationManager,
      GeminiService.layer,
      ZLayer.succeed(networkErrorRef)
    ),

    test("should respect temperature configuration") {
      for
        ref <- ZIO.service[Ref[Option[GeminiRequest]]]
        service <- ZIO.service[GeminiService]
        _       <- service.generateContent("test prompt")
        request <- ref.get
      yield assertTrue(
        request.isDefined,
        request.get.generationConfig.exists(_.temperature.contains(0.7))
      )
    }.provide(
      mockConfig,
      successMockClient,
      mockConversationManager,
      GeminiService.layer,
      ZLayer.succeed(successRef)
    ),

    test("should respect max tokens configuration") {
      for
        ref <- ZIO.service[Ref[Option[GeminiRequest]]]
        service <- ZIO.service[GeminiService]
        _       <- service.generateContent("test prompt")
        request <- ref.get
      yield assertTrue(
        request.isDefined,
        request.get.generationConfig.exists(_.maxOutputTokens.contains(100))
      )
    }.provide(
      mockConfig,
      successMockClient,
      mockConversationManager,
      GeminiService.layer,
      ZLayer.succeed(successRef)
    ),

    suite("conversation history")(
      test("should maintain conversation history") {
        for
          service <- ZIO.service[GeminiService]
          _       <- service.generateContent("First message")
          _       <- service.generateContent("Second message")
          history <- ZIO.service[ConversationManager].flatMap(_.getHistory)
        yield assertTrue(
          history.size == 4, // 2 user messages + 2 model responses
          history(0).role == "model",
          history(1).role == "user",
          history(1).parts.head.text == "Second message",
          history(2).role == "model",
          history(3).role == "user",
          history(3).parts.head.text == "First message"
        )
      }.provide(
        mockConfig,
        successMockClient,
        mockConversationManager,
        GeminiService.layer
      ),

      test("should alternate user and model messages") {
        for
          service <- ZIO.service[GeminiService]
          _       <- service.generateContent("User message")
          history <- ZIO.service[ConversationManager].flatMap(_.getHistory)
        yield assertTrue(
          history.size == 2,
          history(0).role == "model",
          history(0).parts.head.text == "Generated content",
          history(1).role == "user",
          history(1).parts.head.text == "User message"
        )
      }.provide(
        mockConfig,
        successMockClient,
        mockConversationManager,
        GeminiService.layer
      ),

      test("should clear history when requested") {
        for
          service <- ZIO.service[GeminiService]
          _       <- service.generateContent("First message")
          _       <- service.clearHistory
          history <- ZIO.service[ConversationManager].flatMap(_.getHistory)
        yield assertTrue(
          history.isEmpty
        )
      }.provide(
        mockConfig,
        successMockClient,
        mockConversationManager,
        GeminiService.layer
      )
    )
  )

  private def makeHistoryRef = Unsafe.unsafe { implicit u => 
    Runtime.default.unsafe.run(Ref.make(List.empty[GeminiRequest])).getOrThrow()
  }

  private def makeRequestRef = Unsafe.unsafe { implicit u => 
    Runtime.default.unsafe.run(Ref.make(Option.empty[GeminiRequest])).getOrThrow()
  }

  private def makeSuccessMockClient(requestRef: Ref[Option[GeminiRequest]], historyRef: Ref[List[GeminiRequest]]) = ZLayer.succeed {
    new GeminiClient:
      def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] =
        for
          _ <- requestRef.set(Some(request))
          _ <- historyRef.update(request :: _)
          response <- ZIO.succeed(
            GeminiResponse(
              List(
                GeminiCandidate(
                  GeminiContent(
                    List(GeminiPart("Generated content")),
                    "model"
                  )
                )
              ),
              None
            )
          )
        yield response
  }

  private def makeBlockingMockClient(requestRef: Ref[Option[GeminiRequest]], historyRef: Ref[List[GeminiRequest]]) = ZLayer.succeed {
    new GeminiClient:
      def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] =
        for
          _ <- requestRef.set(Some(request))
          _ <- historyRef.update(request :: _)
          response <- ZIO.succeed(
            GeminiResponse(
              List(
                GeminiCandidate(
                  GeminiContent(
                    List(GeminiPart("")),
                    "model"
                  )
                )
              ),
              promptFeedback = Some(
                PromptFeedback(
                  blockReason = Some("Test block reason")
                )
              )
            )
          )
        yield response
  }

  private def makeEmptyResponseMockClient(requestRef: Ref[Option[GeminiRequest]], historyRef: Ref[List[GeminiRequest]]) = ZLayer.succeed {
    new GeminiClient:
      def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] =
        for
          _ <- requestRef.set(Some(request))
          _ <- historyRef.update(request :: _)
          response <- ZIO.succeed(GeminiResponse(List.empty, None))
        yield response
  }

  private def makeNetworkErrorMockClient(requestRef: Ref[Option[GeminiRequest]], historyRef: Ref[List[GeminiRequest]]) = ZLayer.succeed {
    new GeminiClient:
      def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] =
        for
          _ <- requestRef.set(Some(request))
          _ <- historyRef.update(request :: _)
          response <- ZIO.fail(NetworkError(new RuntimeException("Network failure")))
        yield response
  }

  private def makeEmptyPartsMockClient(requestRef: Ref[Option[GeminiRequest]], historyRef: Ref[List[GeminiRequest]]) = ZLayer.succeed {
    new GeminiClient:
      def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] =
        for
          _ <- requestRef.set(Some(request))
          _ <- historyRef.update(request :: _)
          response <- ZIO.succeed(
            GeminiResponse(
              List(
                GeminiCandidate(
                  GeminiContent(List.empty, "model")
                )
              ),
              None
            )
          )
        yield response
  }

  private def makeMissingTextMockClient(requestRef: Ref[Option[GeminiRequest]], historyRef: Ref[List[GeminiRequest]]) = ZLayer.succeed {
    new GeminiClient:
      def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] =
        for
          _ <- requestRef.set(Some(request))
          _ <- historyRef.update(request :: _)
          response <- ZIO.succeed(
            GeminiResponse(
              List(
                GeminiCandidate(
                  GeminiContent(
                    List(GeminiPart(Option.empty[String].orNull)),
                    "model"
                  )
                )
              ),
              None
            )
          )
        yield response
  }

  private val successRef = makeRequestRef
  private val blockingRef = makeRequestRef
  private val emptyResponseRef = makeRequestRef
  private val emptyPartsRef = makeRequestRef
  private val missingTextRef = makeRequestRef
  private val networkErrorRef = makeRequestRef
  private val historyRef = makeHistoryRef

  private val successMockClient = makeSuccessMockClient(successRef, historyRef)
  private val blockingMockClient = makeBlockingMockClient(blockingRef, historyRef)
  private val emptyResponseMockClient = makeEmptyResponseMockClient(emptyResponseRef, historyRef)
  private val networkErrorMockClient = makeNetworkErrorMockClient(networkErrorRef, historyRef)
  private val emptyPartsMockClient = makeEmptyPartsMockClient(emptyPartsRef, historyRef)
  private val missingTextMockClient = makeMissingTextMockClient(missingTextRef, historyRef)

  private val mockConfig = ZLayer.succeed(
    GeminiConfig(
      apiKey = "test-key",
      model = "test-model",
      temperature = 0.7,
      maxTokens = 100,
      retryConfig = RetryConfig(1, 1.second, 5.seconds, 2.0),
      clientTimeout = 30.seconds,
      endpoints = ApiEndpointConfig(
        baseUrl = "http://localhost:8080",
        version = "v1",
        generateContentEndpoint = "generateContent"
      )
    )
  )

  private val mockConversationManager = ZLayer.succeed(
    new ConversationManager:
      private val history = Unsafe.unsafe { implicit u => 
        Runtime.default.unsafe.run(Ref.make(List.empty[GeminiContent])).getOrThrow()
      }
      def addMessage(message: GeminiContent): UIO[Unit] = history.update(message :: _)
      def getHistory: UIO[List[GeminiContent]] = history.get
      def clear: UIO[Unit] = history.set(List.empty)
  )
