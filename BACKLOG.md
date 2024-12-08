# Project Backlog

## Completed ✅

### P0 - Core Functionality
- [x] Basic chat interface with Gemini API
- [x] Configuration management
- [x] Basic test framework
  - Unit tests for core components
  - Integration tests for main flow
  - Mock-based tests for external dependencies
- [x] Error handling and recovery
  - Retry mechanisms for transient failures
  - Improved error messages
  - Graceful interruption handling

### P2 - Developer Experience
- [x] Add scalafmt configuration
  - Scala 3 formatting rules
  - Code style configuration
  - SBT plugin integration
- [x] Improve documentation
  - Added scaladoc comments
  - Added architecture decision records (ADRs)
  - Added API documentation
- [ ] Add scalafix rules
- [ ] GitHub Actions CI Pipeline
  - **Scope**: 
    - Set up GitHub Actions workflow for CI
    - Implement test automation
    - Add code quality checks
  - **Dependencies**: None
  - **Acceptance Criteria**:
    - [ ] Configure GitHub Actions workflow
    - [ ] Run all tests on PR and main
    - [ ] Run scalafmt checks
    - [ ] Run scalafix checks (when added)
    - [ ] Cache dependencies for faster builds
    - [ ] Add build status badge to README
  - **Technical Notes**:
    - Use scala-steward for dependency updates
    - Consider using coursier for faster dependency resolution
    - Add separate jobs for different Scala versions if needed
    - Configure test result reporting
  - **Estimated Effort**: Medium

## In Progress 🚧

### P1 - Enhanced Features
- [ ] Support streaming responses
  - Implement streaming client
  - Add progress indicators
  - Handle partial responses

### P1 - Code Quality
- [ ] Package Structure Refactoring
  - **Scope**: 
    - Replace `com.example` with `io.github.jamesmmiller.ziochat`
    - Update all imports and references
    - Update build.sbt organization
    - Update documentation examples
  - **Dependencies**: None
  - **Acceptance Criteria**:
    - [ ] Update build.sbt organization to `io.github.jamesmmiller`
    - [ ] Update all package declarations to `io.github.jamesmmiller.ziochat`
    - [ ] Update all import statements
    - [ ] Update documentation code examples
    - [ ] All tests passing
    - [ ] Application runs successfully
  - **Technical Notes**:
    - Affects all source files
    - Requires careful testing to ensure no broken imports
    - Consider using scalafix for automated renaming
    - Follow Scala package naming conventions:
      - Lowercase only
      - Reverse domain name notation
      - Project-specific suffix
  - **Estimated Effort**: Medium
- [ ] Improve Test Coverage
  - **Scope**: 
    - Increase overall test coverage
    - Add missing test scenarios
    - Add coverage thresholds to CI
  - **Dependencies**: None
  - **Acceptance Criteria**:
    - [ ] GeminiServiceLive coverage > 80%
      - Error handling scenarios
      - Different response types
      - Content blocking cases
      - Retry scenarios
    - [ ] GeminiModels coverage > 90%
      - JSON encoding/decoding tests
      - Model validation tests
      - Edge cases (empty content, null fields)
    - [ ] Integration Tests
      - Full request/response cycle
      - Error propagation
      - Configuration loading
    - [ ] Add coverage thresholds to CI pipeline
      - Minimum 80% overall coverage
      - Fail build if coverage drops
  - **Technical Notes**:
    - Use scoverage for coverage reporting
    - Add property-based testing for models
    - Consider using test containers for integration tests
    - Add coverage badges to README
  - **Estimated Effort**: Medium

### P1 - Distribution & Packaging
- [ ] Add Homebrew Distribution Support
  - **Scope**: 
    - Enable application distribution via Homebrew
    - Create native binary packaging
    - Automate release process
  - **Dependencies**: None
  - **Sub-tasks**:
    1. Native Binary Packaging
       - [ ] Add GraalVM native-image support
       - [ ] Configure native-image options
       - [ ] Add SBT Universal Plugin
       - [ ] Create binary wrapper scripts
       - [ ] Test native binary packaging

    2. Release Automation
       - [ ] Create GitHub release workflow
       - [ ] Add version management
       - [ ] Configure artifact publishing
       - [ ] Add release documentation
       - [ ] Test release process

    3. Homebrew Formula
       - [ ] Create Homebrew formula
       - [ ] Add formula to tap repository
       - [ ] Configure dependencies
       - [ ] Add installation tests
       - [ ] Document installation process

  - **Acceptance Criteria**:
    - [ ] Application can be installed via `brew install zio-chat`
    - [ ] All dependencies are automatically managed
    - [ ] Native binary works across supported platforms
    - [ ] Release process is fully automated
    - [ ] Installation is documented in README
    - [ ] Version updates are automated

  - **Technical Notes**:
    - Use GraalVM native-image for binary creation
    - Consider cross-platform support (Linux/macOS)
    - Handle API key configuration post-install
    - Use GitHub Actions for release automation
    - Consider using coursier for dependency management
    - Add version command to CLI

  - **Estimated Effort**: Large

## Planned 📋

### P1 - Enhanced Features
- [ ] Add command history (up/down arrows)
- [ ] Save/load conversation history
- [ ] Support conversation branching
- [ ] Add system prompts configuration

### P2 - Developer Experience
- [ ] Add scalafix rules

### P3 - Performance & Monitoring
- [ ] Add metrics collection
  - Request latency
  - Error rates
  - Token usage
- [ ] Add tracing
- [ ] Performance optimization
  - Response caching
  - Connection pooling
  - Request batching

## Nice to Have 🎯
- [ ] Web interface
- [ ] Docker support
- [ ] CI/CD pipeline
- [ ] Release automation
- [ ] Deployment guides

## Notes
- Priority levels: P0 (Must have) > P1 (Should have) > P2 (Could have) > P3 (Won't have this time)
- Items within each priority level are ordered by importance
- Status: Completed ✅, In Progress 🚧, Planned 📋 