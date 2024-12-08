package com.example.service

import zio.*
import com.example.config.GeminiConfig
import com.example.domain.*
import com.example.client.GeminiClient
import com.example.domain.GeminiProtocol.given

/**
 * Service interface for interacting with the Gemini AI model.
 *
 * This service provides high-level operations for generating content and managing
 * conversation history with the Gemini model.
 */
trait GeminiService:
  /**
   * Generates content using the Gemini model.
   *
   * @param prompt
   *   The user's input prompt
   * @return
   *   A ZIO effect that produces the generated response
   */
  def generateContent(prompt: String): IO[GeminiError, String]

  /**
   * Clears the conversation history.
   *
   * @return
   *   A ZIO effect that clears the history
   */
  def clearHistory: UIO[Unit]

/**
 * Live implementation of the GeminiService.
 *
 * @param config
 *   Configuration for the Gemini API
 * @param client
 *   HTTP client for API communication
 * @param conversationManager
 *   Manager for conversation history
 */
case class GeminiServiceLive(
    config: GeminiConfig,
    client: GeminiClient,
    conversationManager: ConversationManager
) extends GeminiService:

  def generateContent(prompt: String): IO[GeminiError, String] =
    for
      userMessage <- ZIO.succeed(GeminiContent(List(GeminiPart(prompt)), "user"))
      _           <- conversationManager.addMessage(userMessage)
      history     <- conversationManager.getHistory
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
      _ <- conversationManager.addMessage(assistantMessage)
      result = response.candidates.head.content.parts.head.text
    yield result

  def clearHistory: UIO[Unit] = conversationManager.clear

/**
 * Companion object for GeminiService providing layer construction.
 */
object GeminiService:

  /**
   * Creates a ZLayer that provides a GeminiService implementation.
   *
   * Requires:
   * - GeminiConfig for API configuration
   * - GeminiClient for API communication
   * - ConversationManager for history management
   */
  val layer: ZLayer[GeminiConfig & GeminiClient & ConversationManager, Nothing, GeminiService] =
    ZLayer {
      for
        config              <- ZIO.service[GeminiConfig]
        client              <- ZIO.service[GeminiClient]
        conversationManager <- ZIO.service[ConversationManager]
      yield GeminiServiceLive(config, client, conversationManager)
    }
