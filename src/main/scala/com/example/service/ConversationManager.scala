package com.example.service

import zio.*
import com.example.domain.GeminiContent

trait ConversationManager:
  def addMessage(content: GeminiContent): UIO[Unit]
  def getHistory: UIO[List[GeminiContent]]
  def clear: UIO[Unit]

case class ConversationManagerLive() extends ConversationManager:
  private var history: List[GeminiContent] = List.empty

  def addMessage(content: GeminiContent): UIO[Unit] = ZIO.succeed {
    history = history :+ content
  }

  def getHistory: UIO[List[GeminiContent]] = ZIO.succeed(history)

  def clear: UIO[Unit] = ZIO.succeed {
    history = List.empty
  }

object ConversationManager:
  val layer: ULayer[ConversationManager] = ZLayer.succeed(ConversationManagerLive()) 