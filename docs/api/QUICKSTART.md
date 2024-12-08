# Quick Start Guide

## Setup

1. Add dependencies to your `build.sbt`:
```scala
libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.0.19",
  "dev.zio" %% "zio-http" % "3.0.0-RC4",
  "dev.zio" %% "zio-json" % "0.6.2"
)
```

2. Create a configuration file (`application.conf`):
```hocon
gemini {
  api-key = ${?GEMINI_API_KEY}
  model = "gemini-pro"
  temperature = 0.7
  max-tokens = 2048
  
  client {
    retry.max-attempts = 3
    retry.initial-delay = 1s
    retry.max-delay = 10s
    retry.backoff-factor = 2.0
    timeout = 30s
  }
}
```

3. Set your API key:
```bash
export GEMINI_API_KEY=your-key-here
```

## Basic Usage

### Simple Chat Application

```scala
import zio.*
import com.example.service.GeminiService
import com.example.config.GeminiConfig

object ChatApp extends ZIOAppDefault:
  def run =
    for
      _ <- Console.printLine("Enter your prompt:")
      prompt <- Console.readLine
      service <- ZIO.service[GeminiService]
      response <- service.generateContent(prompt)
      _ <- Console.printLine(s"Gemini: $response")
    yield ExitCode.success
      .provide(
        GeminiConfig.layer,
        GeminiService.layer
      )
```

### Managing Conversation History

```scala
import com.example.service.ConversationManager

def chatWithHistory =
  for
    service <- ZIO.service[GeminiService]
    manager <- ZIO.service[ConversationManager]
    _ <- service.generateContent("What's the weather?")
    history <- manager.getHistory
    _ <- Console.printLine(s"Chat history: $history")
    _ <- manager.clear  // Clear history when done
  yield ()
```

## Error Handling

### Handling Specific Errors

```scala
import com.example.domain.{GeminiError, ApiError, NetworkError}

def handleErrors(prompt: String) =
  ZIO.service[GeminiService].flatMap(
    _.generateContent(prompt)
      .tapError {
        case ApiError(msg) => 
          Console.printLine(s"API Error: $msg")
        case NetworkError(cause) => 
          Console.printLine(s"Network Error: ${cause.getMessage}")
        case _ => 
          Console.printLine("Unknown error occurred")
      }
  )
```

### Retry Configuration

```scala
import scala.concurrent.duration.*

val retryConfig = RetryConfig(
  maxAttempts = 3,
  initialDelay = 1.second,
  maxDelay = 10.seconds,
  backoffFactor = 2.0
)
```

## Testing

### Mocking Services

```scala
val mockService = new GeminiService:
  def generateContent(prompt: String) = 
    ZIO.succeed("Mock response")
  def clearHistory = ZIO.unit

test("should handle chat interaction") {
  for
    response <- mockService.generateContent("Hello")
  yield assertTrue(response == "Mock response")
}
```

### Integration Tests

```scala
def spec = suite("ChatApp")(
  test("end-to-end chat") {
    for
      _ <- TestConsole.feedLines("Hello")
      _ <- ChatApp.run
      output <- TestConsole.output
    yield assertTrue(output.exists(_.contains("Gemini:")))
  }
)
```

## Best Practices

1. Always handle errors appropriately:
```scala
service.generateContent(prompt)
  .catchAll(handleError)
  .tapError(logError)
```

2. Use proper resource management:
```scala
ZIO.scoped {
  for
    client <- makeClient
    response <- client.use(_.request)
  yield response
}
```

3. Configure timeouts:
```scala
service.generateContent(prompt)
  .timeout(30.seconds)
  .catchAll(_ => ZIO.fail("Request timed out"))
```

4. Handle interruption:
```scala
service.generateContent(prompt)
  .onInterrupt(cleanup)
  .ensuring(finalCleanup)
``` 