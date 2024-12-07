# Contributing Guide

## AI-Assisted Development Workflow

### Using the Backlog with AI

1. **Selecting a Task**
   - Choose a single item from BACKLOG.md
   - Ensure all prerequisites are completed
   - Work on one priority level at a time (complete P0 before P1)

2. **Crafting Effective Prompts**

   Structure your prompts using this template:
   ```
   Task: [Backlog item title]
   
   Context:
   - Current state: [Brief description of current implementation]
   - Dependencies: [List relevant files/components]
   - Constraints: [Any technical or business constraints]
   
   Request:
   [Specific implementation request]
   
   Expected Outcome:
   - [List expected deliverables]
   - [Include acceptance criteria]
   ```

3. **Example Prompts**

   Good prompt:
   ```
   Task: Remove redundant Client.default layer from Main
   
   Context:
   - Current state: Main.scala provides Client.default layer which is already provided by GeminiService
   - Dependencies: Main.scala, GeminiService.scala
   - Constraints: Must maintain ZIO layer composition
   
   Request:
   Update Main.scala to remove the redundant Client.default layer while maintaining proper service composition
   
   Expected Outcome:
   - Cleaned up layer composition in Main.scala
   - No changes to functionality
   - Warning about redundant layer resolved
   ```

   Poor prompt:
   ```
   Fix the Main.scala file
   ```

4. **Best Practices**

   - **Be Specific**: Include exact file names and paths
   - **Provide Context**: Include relevant code snippets or file contents
   - **Set Boundaries**: Clearly define what should and shouldn't be changed
   - **Define Success**: Include clear acceptance criteria
   - **One Task**: Focus on a single backlog item per prompt
   - **Incremental Changes**: Break large tasks into smaller prompts

5. **Review Process**

   Before implementing AI suggestions:
   - Verify the proposed changes match the backlog item
   - Check for unintended side effects
   - Ensure best practices are maintained
   - Validate against acceptance criteria

6. **Documentation Updates**

   After implementing changes:
   - Update BACKLOG.md to mark completed items
   - Document any new configurations or dependencies
   - Update relevant documentation
   - Add comments explaining complex logic

7. **Testing Requirements**

   Each AI-assisted change should:
   - Include or update relevant tests
   - Maintain or improve code coverage
   - Include regression testing
   - Document test scenarios

## Commit Guidelines

1. **Commit Messages**
   ```
   [Type] Short description
   
   - Detailed change 1
   - Detailed change 2
   
   Task: #[Backlog item number]
   AI: Yes/No (indicate if changes were AI-assisted)
   ```

2. **Types**
   - `[Feat]` New features
   - `[Fix]` Bug fixes
   - `[Refactor]` Code restructuring
   - `[Docs]` Documentation updates
   - `[Test]` Test additions/updates
   - `[Config]` Configuration changes

## Getting Help

1. **Refining Prompts**
   - If the AI response isn't helpful, try:
     - Breaking down the task further
     - Providing more specific context
     - Clarifying the expected outcome
     - Including relevant error messages

2. **Common Issues**
   - AI suggests invalid Scala syntax: Specify Scala 3 syntax requirement
   - AI misses dependencies: List all relevant files
   - AI makes too many changes: Break task into smaller prompts
   - AI generates incomplete code: Specify need for complete, compilable solution 