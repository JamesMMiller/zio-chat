# Gemini ZIO Project

A Scala 3 project using ZIO to create an interactive chat application with Google's Gemini AI API.

## Features

- Interactive chat interface with Gemini AI
- Conversation history support
- Type 'exit' to end the chat session
- Built with ZIO for robust, type-safe concurrency

## Prerequisites

- Scala 3.3.1
- SBT 1.9.7
- Google Cloud Gemini API Key

## Setup

1. Get your Gemini API key from Google AI Studio (https://makersuite.google.com/app/apikey)
2. Set the environment variable:
   ```bash
   export GEMINI_API_KEY=your_api_key_here
   ```

## Running the Application

To run the application:

```bash
sbt run
```

The application will start an interactive chat session. You can:
- Enter your prompts and get responses from Gemini
- Chat history is maintained throughout the session
- Type 'exit' to end the session

## Project Structure

- `GeminiService.scala`: Contains the main service for interacting with the Gemini API
  - Handles API communication
  - Maintains chat history
  - JSON encoding/decoding
- `Main.scala`: The main application entry point
  - Implements the interactive chat loop
  - Handles user input/output

## Dependencies

- ZIO 2.0.19: Functional effect system
- ZIO HTTP 3.0.0-RC4: HTTP client
- ZIO JSON 0.6.2: JSON handling

## Potential Improvements

1. Add error handling for network issues
2. Support conversation branching
3. Add command history (up/down arrows)
4. Save/load conversation history
5. Add system prompts configuration
6. Support streaming responses

## Contributing

Feel free to open issues or submit pull requests for improvements. 