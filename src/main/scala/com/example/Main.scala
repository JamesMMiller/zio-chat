package com.example

import zio.*
import zio.http.Client
import zio.Console.*
import com.example.config.GeminiConfig
import zio.http.ZClient
import com.example.service.*
import com.example.client.*
import com.example.domain.*

object Main extends ZIOAppDefault:

  private val commands = Map(
    "exit"  -> "Exit the chat",
    "clear" -> "Clear conversation history",
    "help"  -> "Show available commands"
  )

  def showHelp: UIO[Unit] = printLine(
    s"""
    |Available commands:
    |${commands.map((cmd, desc) => s"  /$cmd - $desc").mkString("\n")}
    """.stripMargin
  ).orDie

  def handleError(error: Throwable): UIO[Unit] = error match
    case e: GeminiError => e match
      case ApiError(message)   => printLine(s"\nAPI Error: $message").orDie
      case NetworkError(cause) => printLine(s"\nNetwork Error: ${cause.getMessage}").orDie
      case ParseError(message) => printLine(s"\nParse Error: $message").orDie
    case e => printLine(s"\nUnexpected error: ${e.getMessage}").orDie

  def processCommand(prompt: String): ZIO[GeminiService, Nothing, Boolean] =
    prompt.trim.toLowerCase match
      case "/exit" => ZIO.succeed(true)
      case "/help" => showHelp *> ZIO.succeed(false)
      case "/clear" => ZIO.serviceWithZIO[GeminiService](_.clearHistory) *>
          printLine("\nConversation history cleared.").orDie *> ZIO.succeed(false)
      case input if input.nonEmpty =>
        ZIO
          .serviceWithZIO[GeminiService](_.generateContent(input))
          .tap(response => printLine(s"\nGemini: $response").orDie)
          .tapError(handleError)
          .ignore *> ZIO.succeed(false)
      case _ => ZIO.succeed(false)

  def chatLoop: ZIO[GeminiService, Nothing, Unit] =
    def loop: ZIO[GeminiService, Nothing, Unit] =
      for
        _      <- printLine("\nEnter your prompt (or /help for commands):").orDie
        prompt <- readLine.orDie
        exit   <- processCommand(prompt)
        _      <- if !exit then loop else ZIO.unit
      yield ()

    loop.onInterrupt(printLine("\nChat session interrupted.").orDie)

  def run: ZIO[Environment & (ZIOAppArgs & Scope), Any, Any] =
    (for
      _ <- printLine(
        """
        |🤖 Welcome to Gemini Chat!
        |Type /help to see available commands.
        |""".stripMargin
      ).orDie
      _ <- chatLoop
    yield ExitCode.success)
      .onInterrupt(
        printLine("\nGoodbye! Chat session ended.").orDie
      )
      .provideSome[Scope](
        GeminiConfig.layer,
        GeminiHttpClient.layer,
        ConversationManager.layer,
        GeminiService.layer,
        ZClient.default
      )
