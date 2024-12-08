package com.example

import zio.*
import zio.test.*
import zio.test.Assertion.*
import com.example.service.*
import com.example.domain.*
import zio.test.TestConsole

object MainSpec extends ZIOSpecDefault:
  def spec = suite("MainSpec")(
    test("should handle help command") {
      for
        fiber  <- Main.chatLoop.fork
        _      <- TestConsole.feedLines("/help", "/exit")
        _      <- fiber.join
        output <- TestConsole.output
      yield assertTrue(
        output.exists(_.contains("Available commands")),
        output.exists(_.contains("/help - Show available commands"))
      )
    }.provide(
      mockGeminiService
    ),

    test("should handle clear command") {
      for
        fiber  <- Main.chatLoop.fork
        _      <- TestConsole.feedLines("/clear", "/exit")
        _      <- fiber.join
        output <- TestConsole.output
      yield assertTrue(output.exists(_.contains("Conversation history cleared")))
    }.provide(
      mockGeminiService
    ),

    test("should handle chat interaction") {
      for
        fiber  <- Main.chatLoop.fork
        _      <- TestConsole.feedLines("Hello", "/exit")
        _      <- fiber.join
        output <- TestConsole.output
      yield assertTrue(output.exists(_.contains("Gemini: Mock response")))
    }.provide(
      mockGeminiService
    ),

    test("should gracefully handle interruption") {
      for
        fiber   <- Main.chatLoop.fork
        _       <- TestConsole.feedLines("Hello")
        _       <- fiber.interrupt
        result  <- fiber.await
      yield assertTrue(result.isInterrupted)
    }.provide(
      mockGeminiService
    )
  )

  private val mockGeminiService = ZLayer.succeed(
    new GeminiService:
      def generateContent(prompt: String): IO[GeminiError, String] =
        ZIO.succeed("Mock response")
      def clearHistory: UIO[Unit] = ZIO.unit
  ) 