package com.example.client

import zio.*
import zio.http.*
import zio.json.*
import com.example.config.GeminiConfig
import com.example.domain.*
import com.example.domain.GeminiProtocol.given
import java.util.concurrent.TimeoutException

trait GeminiClient:
  def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse]

case class GeminiHttpClient(config: GeminiConfig, httpClient: Client) extends GeminiClient:

  def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] =
    for
      url <- ZIO
        .fromEither(
          URL.decode(
            s"https://generativelanguage.googleapis.com/v1/models/${config.model}:generateContent?key=${config.apiKey}"
          )
        )
        .mapError(e => NetworkError(e))
      response <- ZIO.scoped {
        httpClient
          .request(
            Request
              .post(
                url,
                Body.fromString(request.toJson)
              )
              .addHeader(Header.ContentType(MediaType.application.json))
          )
          .retry(Schedule.exponential(1.second) && Schedule.recurs(3))
          .mapError(NetworkError.apply)
          .timeoutFail(NetworkError(new TimeoutException("Request timed out")))(
            Duration.fromScala(config.clientTimeout)
          )
      }
      body <- response.body.asString.mapError(NetworkError.apply)
      _ <- ZIO.when(response.status.code >= 400)(
        ZIO.fail(ApiError(s"API error: ${response.status.code} - $body"))
      )
      geminiResponse <- ZIO
        .fromEither(body.fromJson[GeminiResponse])
        .mapError(e => ParseError(s"Failed to parse response: $e\nResponse: $body"))
      _ <-
        if geminiResponse.promptFeedback.exists(_.blockReason.isDefined) then
          ZIO.fail(
            ApiError(
              s"Content blocked: ${geminiResponse.promptFeedback.flatMap(_.blockReason).getOrElse("Unknown reason")}"
            )
          )
        else ZIO.unit
    yield geminiResponse

  def makeRequest(prompt: String): IO[GeminiError, String] =
    for response <- makeHttpRequest(prompt)
        .retry(
          Schedule.exponential(zio.Duration.fromScala(config.retryConfig.initialDelay)) &&
            Schedule
              .recurWhile[GeminiError](e =>
                e match
                  case NetworkError(_) => true
                  case _               => false
              )
              .upTo(zio.Duration.fromScala(config.retryConfig.maxDelay))
        )
    yield response

  private def makeHttpRequest(prompt: String): IO[GeminiError, String] =
    for
      url <- ZIO.succeed(
        s"https://generativelanguage.googleapis.com/v1/models/${config.model}:generateContent?key=${config.apiKey}"
      )
      response <- ZIO.scoped {
        httpClient
          .request(
            Request
              .post(
                url,
                Body.fromString(prompt)
              )
              .addHeader(Header.ContentType(MediaType.application.json))
          )
          .retry(Schedule.exponential(1.second) && Schedule.recurs(3))
          .mapError(NetworkError.apply)
          .timeoutFail(NetworkError(new TimeoutException("Request timed out")))(
            Duration.fromScala(config.clientTimeout)
          )
      }
      body <- response.body.asString.mapError(NetworkError.apply)
      _ <- ZIO.when(response.status.code >= 400)(
        ZIO.fail(ApiError(s"API error: ${response.status.code} - $body"))
      )
    yield body

object GeminiHttpClient:

  val layer: ZLayer[GeminiConfig & Client, Nothing, GeminiClient] = ZLayer {
    for
      config <- ZIO.service[GeminiConfig]
      client <- ZIO.service[Client]
    yield GeminiHttpClient(config, client)
  }
