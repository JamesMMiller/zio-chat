# Style Guide and AI Prompting

## Repository Overview

This is a ZIO-based Scala project implementing a Gemini AI chat application. The project follows functional programming principles and emphasizes type safety, error handling, and testability.

## Code Style Principles

### Scala Style
```scala
// Preferred style examples
case class Config(
  param1: String,  // Aligned parameters
  param2: Int      // With trailing comma for easy additions
)

def operation[F[_]: Monad](
  param: String
): F[Result] = for {
  a <- stepOne(param)    // Clear step-by-step operations
  b <- stepTwo(a)        // Each step documented if complex
} yield b

sealed trait DomainError  // ADTs for error handling
case class ValidationError(message: String) extends DomainError
```

### ZIO Patterns
```scala
// Preferred ZIO patterns
def businessLogic: ZIO[Env, Error, Result] = for {
  config <- ZIO.service[Config]         // Dependency injection
  _      <- ZIO.logDebug("Starting")    // Structured logging
  result <- operation.retry(Schedule.exponential(1.second))  // Error recovery
} yield result

// Layer composition
val liveLayer = Config.layer >>> Service.layer
```

## Directory Structure
```
src/
├── main/
│   ├── scala/
│   │   └── com/
│   │       └── example/
│   │           ├── config/      # Configuration
│   │           ├── domain/      # Domain models
│   │           ├── service/     # Business logic
│   │           └── Main.scala   # Application entry
│   └── resources/
│       └── reference.conf      # Default configuration
test/
└── scala/
    └── com/
        └── example/
            └── service/        # Test suites
```

## AI Agent Guidelines

### Understanding Tasks

1. **Initial Assessment**
   ```
   Task Analysis Template:
   - Task ID: [From BACKLOG.md]
   - Dependencies: [Required tasks/components]
   - Files to modify: [List of files]
   - New files needed: [List of files]
   ```

2. **Code Context Gathering**
   ```
   Context Gathering Steps:
   1. Check related files in codebase
   2. Review existing patterns
   3. Identify dependencies
   4. Note current implementations
   ```

### Implementation Approach

1. **Code Generation**
   ```scala
   // Always include:
   - Import statements
   - Type signatures
   - Error handling
   - Logging
   - Documentation
   ```

2. **Testing Strategy**
   ```scala
   // Test structure
   class ServiceSpec extends ZIOSpecDefault {
     test("feature description") {
       for {
         result <- operation
         _      <- assertTrue(condition)
       } yield ()
     }
   }
   ```

### Common Patterns

1. **Error Handling**
   ```scala
   sealed trait AppError
   case class ValidationError(msg: String) extends AppError
   case class ServiceError(cause: Throwable) extends AppError
   ```

2. **Configuration**
   ```scala
   case class ServiceConfig(
    param: String,
    timeout: Duration
   )

   object ServiceConfig {
     val layer: ZLayer[Any, Config.Error, ServiceConfig] = ???
   }
   ```

3. **Services**
   ```scala
   trait Service {
     def operation(param: String): IO[ServiceError, Result]
   }

   case class LiveService(config: ServiceConfig) extends Service {
     override def operation(param: String): IO[ServiceError, Result] = ???
   }
   ```

## Task Execution Template

```
Task: [ID from BACKLOG.md]

Context:
- Current Implementation: [Brief description]
- Dependencies: [List files/components]
- Constraints: [Technical/Business]

Changes:
1. File: path/to/file.scala
   - Purpose: [What/Why]
   - Changes: [Specific modifications]

2. File: path/to/test.scala
   - Purpose: [What/Why]
   - Test cases: [List scenarios]

Validation:
- Compilation checks
- Test coverage
- Error handling
- Documentation
```

## Example Task Implementation

```
Task: P0-1.2 Add Scalafmt Configuration

Context:
- Current: No formatting configuration
- Dependencies: build.sbt
- Constraints: Scala 3 syntax

Changes:
1. File: .scalafmt.conf
   - Add version and basic rules
   - Configure Scala 3 settings

2. File: build.sbt
   - Add scalafmt plugin

Validation:
- Run formatter
- Check CI integration
- Update documentation
```

## Common Pitfalls to Avoid

1. **Type Safety**
   - Always handle all error cases
   - Use sealed traits for ADTs
   - Avoid `Any` or `null`

2. **Resource Management**
   - Use `ZIO.scoped` for resources
   - Handle cleanup properly
   - Consider timeouts

3. **Configuration**
   - Don't hardcode values
   - Use TypeSafe Config
   - Environment variables for secrets

4. **Testing**
   - Test error cases
   - Mock external services
   - Use property-based testing

## Commit Message Template

```
[Type] Short description (50 chars)

- Detailed change 1
- Detailed change 2

Task: #[ID]
AI: Yes
```

## Documentation Updates

Always update:
1. README.md for user-facing changes
2. BACKLOG.md for task status
3. Scaladoc for public APIs
4. CHANGELOG.md for releases 