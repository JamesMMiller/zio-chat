package com.example

import zio.*
import zio.json.*
import zio.http.*
import com.example.config.GeminiConfig
import java.util.concurrent.TimeoutException

case class GeminiRequest(
  contents: List[GeminiContent],
  generationConfig: Option[GenerationConfig] = None
)

case class GenerationConfig(
  temperature: Option[Double],
  maxOutputTokens: Option[Int]
)

case class GeminiContent(parts: List[GeminiPart], role: String = "user")
case class GeminiPart(text: String)
case class GeminiResponse(candidates: List[GeminiCandidate], promptFeedback: Option[PromptFeedback] = None)
case class GeminiCandidate(content: GeminiContent)
case class PromptFeedback(blockReason: Option[String])

sealed trait GeminiError extends Throwable
case class ApiError(message: String) extends GeminiError
case class NetworkError(cause: Throwable) extends GeminiError
case class ParseError(message: String) extends GeminiError

object GeminiProtocol:
  given JsonEncoder[GeminiPart] = DeriveJsonEncoder.gen[GeminiPart]
  given JsonEncoder[GeminiContent] = DeriveJsonEncoder.gen[GeminiContent]
  given JsonEncoder[GenerationConfig] = DeriveJsonEncoder.gen[GenerationConfig]
  given JsonEncoder[GeminiRequest] = DeriveJsonEncoder.gen[GeminiRequest]
  given JsonDecoder[GeminiPart] = DeriveJsonDecoder.gen[GeminiPart]
  given JsonDecoder[GeminiContent] = DeriveJsonDecoder.gen[GeminiContent]
  given JsonDecoder[GeminiCandidate] = DeriveJsonDecoder.gen[GeminiCandidate]
  given JsonDecoder[PromptFeedback] = DeriveJsonDecoder.gen[PromptFeedback]
  given JsonDecoder[GeminiResponse] = DeriveJsonDecoder.gen[GeminiResponse]

case class GeminiService(config: GeminiConfig):
  import GeminiProtocol.given
  private var history: List[GeminiContent] = List.empty

  def clearHistory: UIO[Unit] = ZIO.succeed {
    history = List.empty
  }

  def generateContent(prompt: String): IO[GeminiError, String] = ZIO.scoped {
    for
      client <- ZIO.service[Client]
      userMessage = GeminiContent(List(GeminiPart(prompt)), "user")
      _ = history = history :+ userMessage
      generationConfig = GenerationConfig(
        temperature = Some(config.temperature),
        maxOutputTokens = Some(config.maxTokens)
      )
      request = GeminiRequest(history, Some(generationConfig))
      url <- ZIO.fromEither(URL.decode(
        s"https://generativelanguage.googleapis.com/v1/models/${config.model}:generateContent?key=${config.apiKey}"
      )).mapError(e => NetworkError(e))
      response <- client.request(
        Request.post(
          url,
          Body.fromString(request.toJson)
        ).addHeader(Header.ContentType(MediaType.application.json))
      ).mapError(NetworkError.apply)
        .timeoutFail(NetworkError(new TimeoutException("Request timed out")))(Duration.fromScala(config.client.timeout))
      body <- response.body.asString.mapError(NetworkError.apply)
      geminiResponse <- ZIO.fromEither(body.fromJson[GeminiResponse])
        .mapError(e => ParseError(e))
      _ <- ZIO.when(response.status.code >= 400)(
        ZIO.fail(ApiError(s"API error: ${response.status.code} - $body"))
      )
      _ <- ZIO.when(geminiResponse.promptFeedback.exists(_.blockReason.isDefined))(
        ZIO.fail(ApiError(s"Content blocked: ${geminiResponse.promptFeedback.flatMap(_.blockReason).getOrElse("Unknown reason")}"))
      )
      assistantMessage = geminiResponse.candidates.head.content
      _ = history = history :+ assistantMessage
      text = assistantMessage.parts.head.text
    yield text
  }.provideLayer(Client.default.mapError(NetworkError.apply))

object GeminiService:
  val layer: ZLayer[GeminiConfig, Nothing, GeminiService] =
    ZLayer.fromFunction(GeminiService.apply _) 