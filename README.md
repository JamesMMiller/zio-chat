# Gemini ZIO Chat

A modern Scala chat application using Google's Gemini AI model, built with ZIO for robust, type-safe concurrent programming.

## Features

- Interactive chat interface with Gemini AI
- Type-safe error handling
- Configurable retry mechanisms
- Structured logging
- Environment-based configuration

## Prerequisites

- JDK 17+
- SBT 1.9.7+
- Google Cloud Gemini API Key

## Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd gemini-zio-project
   ```

2. **Set up environment**
   ```bash
   export GEMINI_API_KEY=your-api-key
   ```

3. **Run the application**
   ```bash
   sbt run
   ```

## Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── scala/com/example/
│   │   │   ├── config/          # Configuration management
│   │   │   ├── GeminiService.scala  # Core service
│   │   │   └── Main.scala       # Application entry
│   │   └── resources/
│   │       └── reference.conf   # Default configuration
│   └── test/
│       └── scala/com/example/   # Test suites
├── project/                     # SBT configuration
├── .gitignore                  # Git ignore rules
├── BACKLOG.md                  # Project roadmap
├── CONTRIBUTING.md             # Contribution guidelines
├── GIT.md                      # Git workflow
└── STYLE_AND_PROMPTS.md        # Style guide & AI prompts
```

## Configuration

The application uses TypeSafe Config for configuration management. Key configurations:

```hocon
gemini {
  api-key = ${?GEMINI_API_KEY}
  model = "gemini-pro"
  temperature = 0.7
  max-tokens = 2048
  
  client {
    retry {
      max-attempts = 3
      initial-delay = 1s
      max-delay = 10s
      backoff-factor = 2.0
    }
    timeout = 30s
  }
}
```

## Development

This project follows a structured development process with AI assistance:

1. **Task Selection**
   - Tasks are tracked in BACKLOG.md
   - Each task has clear acceptance criteria
   - Follow priority order (P0 → P1 → P2)

2. **Implementation**
   - Follow STYLE_AND_PROMPTS.md guidelines
   - Use provided templates for consistency
   - Ensure proper testing and documentation

3. **Git Workflow**
   - Feature branches from develop
   - PR reviews required
   - Automated checks and formatting

## Contributing

We welcome contributions! Please see:
- CONTRIBUTING.md for process guidelines
- STYLE_AND_PROMPTS.md for coding standards
- GIT.md for version control workflow
- BACKLOG.md for planned features

## Available Commands

In the chat interface:
- `/help` - Show available commands
- `/clear` - Clear conversation history
- `/exit` - Exit the application

## Error Handling

The application handles various error scenarios:
- Network issues
- API rate limiting
- Timeouts
- Content filtering
- Parsing errors

## Testing

Run tests with:
```bash
sbt test
```

## License

[License Type] - See LICENSE file for details

## Acknowledgments

- ZIO Team for the excellent functional effects system
- Google Cloud for the Gemini AI API
- Contributors and maintainers 