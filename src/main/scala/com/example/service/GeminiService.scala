package com.example.service

import zio.*
import com.example.config.GeminiConfig
import com.example.domain.*
import com.example.client.GeminiClient
import com.example.domain.GeminiProtocol.given

trait GeminiService:
  def generateContent(prompt: String): IO[GeminiError, String]
  def clearHistory: UIO[Unit]

case class GeminiServiceLive(
    config: GeminiConfig,
    client: GeminiClient,
    conversationManager: ConversationManager
) extends GeminiService:

  def generateContent(prompt: String): IO[GeminiError, String] =
    for
      userMessage <- ZIO.succeed(GeminiContent(List(GeminiPart(prompt)), "user"))
      _          <- conversationManager.addMessage(userMessage)
      history    <- conversationManager.getHistory
      generationConfig = GenerationConfig(
        temperature = Some(config.temperature),
        maxOutputTokens = Some(config.maxTokens)
      )
      request = GeminiRequest(history, Some(generationConfig))
      response <- client.generateContent(request)
      _ <- ZIO.when(response.promptFeedback.exists(_.blockReason.isDefined))(
        ZIO.fail(
          ApiError(
            s"Content blocked: ${response.promptFeedback.flatMap(_.blockReason).getOrElse("Unknown reason")}"
          )
        )
      )
      assistantMessage = response.candidates.head.content
      _      <- conversationManager.addMessage(assistantMessage)
      result = response.candidates.head.content.parts.head.text
    yield result

  def clearHistory: UIO[Unit] = conversationManager.clear

object GeminiService:
  val layer: ZLayer[GeminiConfig & GeminiClient & ConversationManager, Nothing, GeminiService] =
    ZLayer {
      for
        config <- ZIO.service[GeminiConfig]
        client <- ZIO.service[GeminiClient]
        conversationManager <- ZIO.service[ConversationManager]
      yield GeminiServiceLive(config, client, conversationManager)
    } 