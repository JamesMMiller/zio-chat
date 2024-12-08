package com.example.service

import zio.*
import zio.test.*
import zio.test.Assertion.*
import com.example.domain.*

object ConversationManagerSpec extends ZIOSpecDefault:

  def spec = suite("ConversationManager")(
    test("should start with empty history") {
      for
        manager <- ZIO.service[ConversationManager]
        history <- manager.getHistory
      yield assertTrue(history.isEmpty)
    }.provide(ConversationManager.layer),
    test("should add messages to history") {
      val message1 = GeminiContent(List(GeminiPart("Hello")), "user")
      val message2 = GeminiContent(List(GeminiPart("Hi there")), "model")

      for
        manager <- ZIO.service[ConversationManager]
        _       <- manager.addMessage(message1)
        _       <- manager.addMessage(message2)
        history <- manager.getHistory
      yield assertTrue(
        history == List(message1, message2)
      )
    }.provide(ConversationManager.layer),
    test("should maintain message order") {
      val messages = List(
        GeminiContent(List(GeminiPart("1")), "user"),
        GeminiContent(List(GeminiPart("2")), "model"),
        GeminiContent(List(GeminiPart("3")), "user")
      )

      for
        manager <- ZIO.service[ConversationManager]
        _       <- ZIO.foreach(messages)(manager.addMessage)
        history <- manager.getHistory
      yield assertTrue(
        history == messages,
        history.map(_.parts.head.text) == List("1", "2", "3")
      )
    }.provide(ConversationManager.layer),
    test("should clear history") {
      val message = GeminiContent(List(GeminiPart("Test")), "user")

      for
        manager <- ZIO.service[ConversationManager]
        _       <- manager.addMessage(message)
        _       <- manager.clear
        history <- manager.getHistory
      yield assertTrue(history.isEmpty)
    }.provide(ConversationManager.layer),
    test("should handle multiple operations") {
      val message1 = GeminiContent(List(GeminiPart("First")), "user")
      val message2 = GeminiContent(List(GeminiPart("Second")), "model")

      for
        manager <- ZIO.service[ConversationManager]
        _       <- manager.addMessage(message1)
        _       <- manager.clear
        _       <- manager.addMessage(message2)
        history <- manager.getHistory
      yield assertTrue(
        history == List(message2)
      )
    }.provide(ConversationManager.layer)
  )
