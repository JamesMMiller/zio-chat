package com.example

import zio.*
import zio.http.Client
import zio.Console.*
import com.example.config.GeminiConfig

object Main extends ZIOAppDefault:
  private val commands = Map(
    "exit" -> "Exit the chat",
    "clear" -> "Clear conversation history",
    "help" -> "Show available commands"
  )

  def showHelp: UIO[Unit] = printLine(
    s"""
    |Available commands:
    |${commands.map((cmd, desc) => s"  /$cmd - $desc").mkString("\n")}
    """.stripMargin
  ).orDie

  def handleError(error: GeminiError): UIO[Unit] = error match
    case ApiError(message) => 
      printLine(s"\nAPI Error: $message").orDie
    case NetworkError(cause) => 
      printLine(s"\nNetwork Error: ${cause.getMessage}").orDie
    case ParseError(message) => 
      printLine(s"\nParse Error: $message").orDie

  def chatLoop: ZIO[GeminiService, Nothing, Unit] = for
    _ <- printLine("\nEnter your prompt (or /help for commands):").orDie
    prompt <- readLine.orDie
    _ <- prompt.trim.toLowerCase match
      case "/exit" => ZIO.unit
      case "/help" => showHelp *> chatLoop
      case "/clear" => ZIO.serviceWithZIO[GeminiService](_.clearHistory) *> 
        printLine("\nConversation history cleared.").orDie *> chatLoop
      case input if input.nonEmpty => 
        ZIO.serviceWithZIO[GeminiService](_.generateContent(input))
          .tap(response => printLine(s"\nGemini: $response").orDie)
          .catchAll(handleError)
          .as(()) *> chatLoop
      case _ => chatLoop
  yield ()

  def run = for
    _ <- printLine(
      """
      |🤖 Welcome to Gemini Chat!
      |Type /help to see available commands.
      |""".stripMargin
    ).orDie
    _ <- chatLoop.provide(
      GeminiConfig.layer,
      GeminiService.layer
    )
  yield () 