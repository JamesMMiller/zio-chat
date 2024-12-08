package com.example.domain

import zio.json.*

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

case class GeminiResponse(
    candidates: List[GeminiCandidate],
    promptFeedback: Option[PromptFeedback] = None
)

case class GeminiCandidate(content: GeminiContent)
case class PromptFeedback(blockReason: Option[String])

sealed trait GeminiError extends Throwable
case class ApiError(message: String) extends GeminiError
case class NetworkError(cause: Throwable) extends GeminiError
case class ParseError(message: String) extends GeminiError

object GeminiProtocol:
  given JsonEncoder[GeminiPart]       = DeriveJsonEncoder.gen[GeminiPart]
  given JsonEncoder[GeminiContent]    = DeriveJsonEncoder.gen[GeminiContent]
  given JsonEncoder[GenerationConfig] = DeriveJsonEncoder.gen[GenerationConfig]
  given JsonEncoder[GeminiRequest]    = DeriveJsonEncoder.gen[GeminiRequest]
  given JsonEncoder[GeminiCandidate]  = DeriveJsonEncoder.gen[GeminiCandidate]
  given JsonEncoder[PromptFeedback]   = DeriveJsonEncoder.gen[PromptFeedback]
  given JsonEncoder[GeminiResponse]   = DeriveJsonEncoder.gen[GeminiResponse]
  given JsonDecoder[GeminiPart]       = DeriveJsonDecoder.gen[GeminiPart]
  given JsonDecoder[GeminiContent]    = DeriveJsonDecoder.gen[GeminiContent]
  given JsonDecoder[GeminiCandidate]  = DeriveJsonDecoder.gen[GeminiCandidate]
  given JsonDecoder[PromptFeedback]   = DeriveJsonDecoder.gen[PromptFeedback]
  given JsonDecoder[GeminiResponse]   = DeriveJsonDecoder.gen[GeminiResponse] 