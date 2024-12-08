# Gemini ZIO Project

A Scala project using ZIO to interact with Google's Gemini API.

## Features

- Interactive chat with Gemini AI
- Command-based interface (/help, /clear, /exit)
- Conversation history management
- Robust error handling and retries
- Comprehensive test coverage

## Getting Started

### Prerequisites

- Scala 3.3.1
- SBT 1.9.7
- Java 17 or higher
- Gemini API key (get one from [Google AI Studio](https://makersuite.google.com/app/apikey))

### Running the Application

1. Set your API key:
```bash
export GEMINI_API_KEY=your-api-key-here
```

2. Run the application:
```bash
sbt run
```

### Running Tests

Run all tests:
```bash
sbt test
```

## Project Structure

```
src/
├── main/scala/com/example/
│   ├── client/          # HTTP client implementation
│   ├── config/          # Configuration handling
│   ├── domain/          # Domain models
│   ├── service/         # Business logic
│   └── Main.scala       # Application entry point
└── test/scala/com/example/
    ├── client/          # Client tests
    ├── service/         # Service tests
    └── MainSpec.scala   # Integration tests
```

## Testing Strategy

The project includes several types of tests:
- Unit tests for individual components
- Integration tests for the main application flow
- Mock-based tests for external dependencies
- Property-based tests for data models

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines. 

### Pull Request Process
1. Use the provided PR template when creating pull requests
2. Follow the conventional commits specification
3. Ensure all tests are passing
4. Update documentation as needed 