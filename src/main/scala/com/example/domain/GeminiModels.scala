package com.example.domain

import zio.json.*

/** Request model for the Gemini API.
  *
  * @param contents
  *   List of content parts to be processed by the model
  * @param generationConfig
  *   Optional configuration for text generation
  */
case class GeminiRequest(
    contents: List[GeminiContent],
    generationConfig: Option[GenerationConfig] = None
)

/** Configuration for text generation.
  *
  * @param temperature
  *   Controls randomness in the response (0.0 to 1.0)
  * @param maxOutputTokens
  *   Maximum number of tokens to generate
  */
case class GenerationConfig(
    temperature: Option[Double],
    maxOutputTokens: Option[Int]
)

/** Content part of a Gemini request or response.
  *
  * @param parts
  *   List of text parts in the content
  * @param role
  *   Role of the content (e.g., "user" or "model")
  */
case class GeminiContent(parts: List[GeminiPart], role: String = "user")

/** Individual text part in a content block.
  *
  * @param text
  *   The actual text content
  */
case class GeminiPart(text: String)

/** Response model from the Gemini API.
  *
  * @param candidates
  *   List of generated responses
  * @param promptFeedback
  *   Optional feedback about the prompt (e.g., content filtering)
  */
case class GeminiResponse(
    candidates: List[GeminiCandidate],
    promptFeedback: Option[PromptFeedback] = None
)

/** Individual candidate response from the model.
  *
  * @param content
  *   The generated content
  */
case class GeminiCandidate(content: GeminiContent)

/** Feedback about prompt processing.
  *
  * @param blockReason
  *   Optional reason why content was blocked
  */
case class PromptFeedback(blockReason: Option[String])

/** Base trait for all Gemini-related errors.
  */
sealed trait GeminiError extends Throwable

/** Error returned by the Gemini API.
  *
  * @param message
  *   The error message from the API
  */
case class ApiError(message: String) extends GeminiError

/** Network-related errors during API communication.
  *
  * @param cause
  *   The underlying cause of the network error
  */
case class NetworkError(cause: Throwable) extends GeminiError

/** Error parsing API responses.
  *
  * @param message
  *   Description of what failed to parse
  */
case class ParseError(message: String) extends GeminiError

/** JSON encoders and decoders for Gemini models.
  */
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