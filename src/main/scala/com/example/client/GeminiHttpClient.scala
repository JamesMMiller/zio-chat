package com.example.client

import zio.*
import zio.http.*
import zio.json.*
import com.example.config.GeminiConfig
import com.example.domain.*
import com.example.domain.GeminiProtocol.given

trait GeminiClient:
  def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse]

final case class GeminiHttpClient(config: GeminiConfig, client: Client) extends GeminiClient:

  private def buildRequest(request: GeminiRequest): IO[GeminiError, Request] =
    val urlString =
      s"${config.endpoints.baseUrl}/${config.endpoints.version}/models/${config.model}:${config.endpoints.generateContentEndpoint}?key=${config.apiKey}"

    ZIO
      .fromEither(URL.decode(urlString))
      .mapError(NetworkError(_))
      .map { url =>
        Request(
          url = url,
          method = Method.POST,
          body = Body.fromString(request.toJson),
          headers = Headers(Header.ContentType(MediaType.application.json)),
          version = Version.Http_1_1,
          remoteAddress = None
        )
      }

  override def generateContent(request: GeminiRequest): IO[GeminiError, GeminiResponse] =
    for
      req      <- buildRequest(request)
      response <- client.request(req).mapError(NetworkError(_))
      body     <- response.body.asString.mapError(NetworkError(_))
      _ <- ZIO.when(response.status.code >= 400)(
        ZIO.fail(ApiError(s"API error: ${response.status.code} - $body"))
      )
      result <- ZIO
        .fromEither(body.fromJson[GeminiResponse])
        .mapError(e => ParseError(s"Failed to parse response: $e\nResponse: $body"))
    yield result

object GeminiHttpClient:

  val layer: ZLayer[GeminiConfig & Client, Nothing, GeminiClient] = ZLayer {
    for
      config <- ZIO.service[GeminiConfig]
      client <- ZIO.service[Client]
    yield GeminiHttpClient(config, client)
  }
