package com.example.client

import zio.test.*
import zio.test.Assertion.*
import zio.*
import zio.json.*
import zio.http.*
import zio.http.Body
import zio.http.Header.ContentType
import zio.http.MediaType
import com.example.config.*
import com.example.domain.*
import com.example.domain.GeminiProtocol.given
import scala.concurrent.duration.*

object GeminiHttpClientSpec extends ZIOSpecDefault {

  private val testRequest = GeminiRequest(List(GeminiContent(List(GeminiPart("test prompt")))))
  private val testResponse = GeminiResponse(List(GeminiCandidate(GeminiContent(List(GeminiPart("test response"))))))

  val testConfig = GeminiConfig(
    apiKey = "test-key",
    model = "test-model",
    temperature = 0.7,
    maxTokens = 100,
    retryConfig = RetryConfig(1, 1.second, 5.seconds, 2.0),
    clientTimeout = 30.seconds,
    endpoints = ApiEndpointConfig(
      baseUrl = "http://localhost:8080",
      version = "v1",
      generateContentEndpoint = "generate"
    )
  )

  def mockClientWithResponse(response: Response): ULayer[Client] =
  ZLayer.succeed(
    new Client {
      def headers: Headers = Headers.empty
      def method: Method = Method.GET
      def url: URL = URL.decode("http://localhost").toOption.get
      def version: Version = Version.Http_1_1
      def sslConfig: Option[ClientSSLConfig] = None

      def request(
        version: Version,
        method: Method,
        url: URL,
        headers: Headers,
        body: Body,
        sslConfig: Option[ClientSSLConfig]
      )(implicit trace: Trace): ZIO[Any, Throwable, Response] =
        ZIO.succeed(response)

      def socket[Env1](
        version: Version,
        url: URL,
        headers: Headers,
        app: SocketApp[Env1]
      )(implicit trace: Trace): ZIO[Env1 & Scope, Throwable, Response] =
        ZIO.dieMessage("WebSocket not supported in this mock")
    }
  )

def mockClientWithFailure(t: Throwable): ULayer[Client] =
  ZLayer.succeed(
    new Client {
      def headers: Headers = Headers.empty
      def method: Method = Method.GET
      def url: URL = URL.decode("http://localhost").toOption.get
      def version: Version = Version.Http_1_1
      def sslConfig: Option[ClientSSLConfig] = None

      def request(
        version: Version,
        method: Method,
        url: URL,
        headers: Headers,
        body: Body,
        sslConfig: Option[ClientSSLConfig]
      )(implicit trace: Trace): ZIO[Any, Throwable, Response] =
        ZIO.fail(t)

      def socket[Env1](
        version: Version,
        url: URL,
        headers: Headers,
        app: SocketApp[Env1]
      )(implicit trace: Trace): ZIO[Env1 & Scope, Throwable, Response] =
        ZIO.dieMessage("WebSocket not supported in this mock")
    }
  )


  override def spec = suite("GeminiHttpClientSpec")(
    test("should return GeminiResponse on successful request") {
      val bodyString = testResponse.toJson
      val successResponse = Response(
        status = Status.Ok,
        headers = Headers(ContentType(MediaType.application.json)),
        body = Body.fromString(bodyString)
      )

      val layer = ZLayer.succeed(testConfig) ++ mockClientWithResponse(successResponse) >>> GeminiHttpClient.layer

      for {
        client <- ZIO.service[GeminiClient]
        result <- client.generateContent(testRequest)
      } yield assertTrue(result == testResponse)
    }.provideLayer(ZLayer.succeed(testConfig) ++ mockClientWithResponse(
      Response(
        status = Status.Ok,
        headers = Headers(ContentType(MediaType.application.json)),
        body = Body.fromString(testResponse.toJson)
      )
    ) >>> GeminiHttpClient.layer),

    test("should fail with ApiError if status code is >= 400") {
      val errorBody = """{ "error": "Bad Request" }"""
      val errorResponse = Response(
        status = Status.BadRequest,
        headers = Headers(ContentType(MediaType.application.json)),
        body = Body.fromString(errorBody)
      )

      val layer = ZLayer.succeed(testConfig) ++ mockClientWithResponse(errorResponse) >>> GeminiHttpClient.layer

      for {
        client <- ZIO.service[GeminiClient]
        result <- client.generateContent(testRequest).exit
      } yield assert(result)(fails(isSubtype[ApiError](anything)))
    }.provideLayer(ZLayer.succeed(testConfig) ++ mockClientWithResponse(
      Response(
        status = Status.BadRequest,
        headers = Headers(ContentType(MediaType.application.json)),
        body = Body.fromString("""{ "error": "Bad Request" }""")
      )
    ) >>> GeminiHttpClient.layer),

    test("should fail with ParseError if response cannot be parsed") {
      val invalidJson = """{ "invalid_json": """
      val invalidJsonResponse = Response(
        status = Status.Ok,
        headers = Headers(ContentType(MediaType.application.json)),
        body = Body.fromString(invalidJson)
      )

      val layer = ZLayer.succeed(testConfig) ++ mockClientWithResponse(invalidJsonResponse) >>> GeminiHttpClient.layer

      for {
        client <- ZIO.service[GeminiClient]
        result <- client.generateContent(testRequest).exit
      } yield assert(result)(fails(isSubtype[ParseError](anything)))
    }.provideLayer(ZLayer.succeed(testConfig) ++ mockClientWithResponse(
      Response(
        status = Status.Ok,
        headers = Headers(ContentType(MediaType.application.json)),
        body = Body.fromString("""{ "invalid_json": """)
      )
    ) >>> GeminiHttpClient.layer),

    test("should fail with NetworkError if client fails") {
      for {
        client <- ZIO.service[GeminiClient]
        result <- client.generateContent(testRequest).exit
      } yield assert(result)(fails(isSubtype[NetworkError](anything)))
    }.provideLayer(ZLayer.succeed(testConfig) ++ mockClientWithFailure(new RuntimeException("Network failure")) >>> GeminiHttpClient.layer),

    test("should fail with NetworkError if URL is invalid") {
      // Create a config with an invalid URL to trigger URL decoding failure
      val invalidConfig = testConfig.copy(endpoints = testConfig.endpoints.copy(baseUrl = "http:// invalid-url"))

      val layer = ZLayer.succeed(invalidConfig) ++ mockClientWithResponse(Response.ok) >>> GeminiHttpClient.layer

      for {
        client <- ZIO.service[GeminiClient]
        result <- client.generateContent(testRequest).exit
      } yield assert(result)(fails(isSubtype[NetworkError](anything)))
    }.provideLayer(ZLayer.succeed(testConfig.copy(endpoints = testConfig.endpoints.copy(baseUrl = "http:// invalid-url"))) ++ mockClientWithResponse(Response.ok) >>> GeminiHttpClient.layer)
  )
}
