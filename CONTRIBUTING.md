# Contributing Guide

## Overview

This project uses AI-assisted development while maintaining high code quality standards. All contributions should follow the established patterns and guidelines.

## Key Documents

Before contributing, familiarize yourself with:
- `STYLE_AND_PROMPTS.md` - Code style and AI interaction guidelines
- `GIT.md` - Git workflow and commit standards
- `BACKLOG.md` - Project roadmap and task tracking
- `README.md` - Project overview and setup

## Development Workflow

1. **Task Selection**
   - Choose tasks from BACKLOG.md
   - Follow priority order (P0 → P1 → P2)
   - Ensure prerequisites are completed
   - Update task status in BACKLOG.md

   **Backlog Item Format**
   ```markdown
   ### Priority Level (P0/P1/P2/P3)
   - [ ] Task Title
     - **Scope**: What's included/excluded
     - **Dependencies**: Required tasks/components
     - **Acceptance Criteria**:
       - [ ] Criterion 1
       - [ ] Criterion 2
     - **Technical Notes**:
       - Implementation details
       - Architecture considerations
     - **Estimated Effort**: Small/Medium/Large
   ```

2. **Development Process**
   - Create feature branch from develop
   - Follow style guide in STYLE_AND_PROMPTS.md
   - Use AI assistance effectively
   - Write tests for new features
   - Update documentation

3. **Code Quality**
   - Follow Scala 3 best practices
   - Maintain type safety
   - Handle errors appropriately
   - Add proper logging
   - Include documentation

## AI-Assisted Development

### Using the AI Agent

1. **Task Preparation**
   - Check for open PRs:
     ```bash
     gh pr list
     ```
   - If PRs exist, review and handle them before starting new work
   - Select task from BACKLOG.md following priority order

2. **Task Format**
   ```
   Task: [Backlog item title]
   
   Context:
   - Current state: [Brief description]
   - Dependencies: [List relevant files]
   - Constraints: [Technical/business]
   
   Request:
   [Specific implementation request]
   
   Expected Outcome:
   - [List deliverables]
   - [Include acceptance criteria]
   ```

3. **Best Practices**
   - Be specific in requests
   - Provide context
   - Set clear boundaries
   - Define success criteria
   - Focus on single tasks
   - Use incremental changes

4. **Review Process**
   - Verify changes match requirements
   - Check for side effects
   - Validate best practices
   - Confirm acceptance criteria

## Local Development

1. **Prerequisites**
   - JDK 17+
   - SBT 1.9.7+
   - Git
   - IDE (recommended: IntelliJ with Scala plugin)

2. **Environment Setup**
   ```bash
   # Clone repository
   git clone <repo-url>
   cd gemini-zio-project

   # Set up environment
   cp .env.example .env
   # Edit .env with your settings

   # Install git hooks
   cp .git-hooks/* .git/hooks/
   chmod +x .git/hooks/*
   ```

3. **Build and Test**
   ```bash
   sbt clean compile
   sbt test
   ```

## Guidelines

### Debugging
- Use structured logging
- Include correlation IDs
- Handle all error cases
- Test error scenarios

### Performance
- Avoid blocking operations
- Use appropriate data structures
- Consider memory usage
- Profile before optimizing

### Security
- Validate inputs
- Secure configurations
- Regular dependency updates
- Proper error messages

## Pull Request Process

1. **Preparation**
   - Update from develop
   - Run all tests
   - Update documentation
   - Check acceptance criteria

2. **Creating PR (GitHub CLI)**
   ```bash
   # Create PR template
   cat > .pr-template.md << 'EOF'
   ## Description
   Brief description of changes

   ## Task Reference
   - Task: #[ID]
   - AI Assisted: Yes/No

   ## Acceptance Criteria
   - [ ] Criterion 1
   - [ ] Criterion 2

   ## Testing
   - [ ] Unit tests
   - [ ] Integration tests
   - [ ] Manual testing

   ## Documentation
   - [ ] Code comments
   - [ ] README updates
   - [ ] BACKLOG.md updates
   EOF

   # Push your branch
   git push -u origin feature/your-branch-name

   # Create PR using gh cli
   gh pr create \
     --title "[Type] Short description" \
     --body-file .pr-template.md

   # Clean up template
   rm .pr-template.md
   ```

3. **Review Process**
   ```bash
   # Check PR status
   gh pr status

   # List PRs
   gh pr list

   # Review PR
   gh pr review [PR-number] --approve

   # Merge PR (when approved)
   gh pr merge [PR-number] --squash --delete-branch
   ```

## Getting Help

1. **Common Issues**
   - Check existing documentation
   - Review similar PRs
   - Ask in discussions
   - Use AI assistance effectively

2. **AI Assistance Tips**
   - Break down complex tasks
   - Provide clear context
   - Include error messages
   - Specify exact requirements

## Documentation

Always update:
1. Code comments
2. README.md for user-facing changes
3. BACKLOG.md for task status
4. CHANGELOG.md for releases
5. API documentation