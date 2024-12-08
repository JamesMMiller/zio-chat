package com.example.service

import zio.*
import com.example.domain.GeminiContent

trait ConversationManager:
  def addMessage(content: GeminiContent): UIO[Unit]
  def getHistory: UIO[List[GeminiContent]]
  def clear: UIO[Unit]

case class ConversationManagerLive(history: Ref[List[GeminiContent]]) extends ConversationManager:
  def addMessage(content: GeminiContent): UIO[Unit] =
    history.update(_ :+ content)

  def getHistory: UIO[List[GeminiContent]] =
    history.get

  def clear: UIO[Unit] =
    history.set(List.empty)

object ConversationManager:
  val layer: ULayer[ConversationManager] = ZLayer {
    for
      history <- Ref.make(List.empty[GeminiContent])
    yield ConversationManagerLive(history)
  }
