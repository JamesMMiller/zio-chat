# Gemini ZIO Project API Documentation

## Overview

This document describes the core APIs and components of the Gemini ZIO Project.

## Core Components

### GeminiService

The main service for interacting with the Gemini AI model.

```scala
trait GeminiService:
  def generateContent(prompt: String): IO[GeminiError, String]
  def clearHistory: UIO[Unit]
```

#### Methods

- `generateContent(prompt: String)`: Generates AI content from a prompt
  - Parameters:
    - `prompt`: The user's input text
  - Returns: `IO[GeminiError, String]` containing the model's response
  - Errors:
    - `ApiError`: When the API returns an error
    - `NetworkError`: For connection issues
    - `ParseError`: For response parsing failures

- `clearHistory()`: Clears the conversation history
  - Returns: `UIO[Unit]` that always succeeds

### ConversationManager

Manages the chat conversation history.

```scala
trait ConversationManager:
  def addMessage(content: GeminiContent): UIO[Unit]
  def getHistory: UIO[List[GeminiContent]]
  def clear: UIO[Unit]
```

#### Methods

- `addMessage(content)`: Adds a message to history
- `getHistory()`: Retrieves all conversation messages
- `clear()`: Clears all history

### Configuration

The application uses HOCON configuration with the following structure:

```hocon
gemini {
  api-key = ${?GEMINI_API_KEY}  # From environment variable
  model = "gemini-pro"          # Model name
  temperature = 0.7             # Response randomness
  max-tokens = 2048             # Max response length
  
  client {
    retry {
      max-attempts = 3          # Max retry attempts
      initial-delay = 1s        # Initial retry delay
      max-delay = 10s          # Maximum retry delay
      backoff-factor = 2.0     # Exponential backoff
    }
    timeout = 30s              # Request timeout
  }
}
```

## Error Handling

The application uses a custom error hierarchy:

```scala
sealed trait GeminiError extends Throwable
case class ApiError(message: String) extends GeminiError
case class NetworkError(cause: Throwable) extends GeminiError
case class ParseError(message: String) extends GeminiError
```

### Error Recovery

- Network errors are automatically retried with exponential backoff
- API errors include detailed messages from Gemini
- Parse errors include the problematic response

## Usage Examples

### Basic Chat Interaction

```scala
for
  service <- ZIO.service[GeminiService]
  response <- service.generateContent("Tell me a joke")
  _ <- Console.printLine(s"Gemini: $response")
yield ()
```

### Managing Conversation History

```scala
for
  service <- ZIO.service[GeminiService]
  _ <- service.generateContent("Hello")
  _ <- service.generateContent("How are you?")
  _ <- service.clearHistory  // Clear history
yield ()
```

## Dependencies

The project uses the following core dependencies:
- ZIO 2.0.19: Core runtime and effects
- ZIO HTTP 3.0.0-RC4: HTTP client
- ZIO JSON 0.6.2: JSON handling
- Typesafe Config 1.4.3: Configuration

## Best Practices

1. **Error Handling**
   - Always handle potential errors using ZIO's error channel
   - Use `orDie` only for truly unrecoverable errors
   - Provide meaningful error messages

2. **Resource Management**
   - Use `ZIO.scoped` for managing resources
   - Clean up resources in the reverse order of acquisition
   - Use `onInterrupt` for graceful interruption

3. **Configuration**
   - Never hardcode sensitive values
   - Use environment variables for secrets
   - Provide clear error messages for missing configuration

4. **Testing**
   - Write tests for both success and failure cases
   - Use mocks for external dependencies
   - Test error recovery mechanisms 