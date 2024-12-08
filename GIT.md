# Git Workflow Guidelines

## Branch Strategy

### Main Branches
- `main` - Production-ready code
- `develop` - Integration branch for features

### Feature Branches
- Format: `feature/P0-1-1-remove-client-layer`
- Components:
  - `feature/` - Type prefix
  - `P0-1-1` - Task ID from BACKLOG.md
  - `-description` - Brief description

### Other Branch Types
- `bugfix/` - For bug fixes
- `hotfix/` - For urgent production fixes
- `release/` - For release preparation

## Commit Guidelines

### Commit Message Format
```
[Type] Short description (50 chars)

- Detailed change 1
- Detailed change 2

Task: #P0-1-1
AI: Yes/No
```

### Commit Types
- `[Feat]` - New features
- `[Fix]` - Bug fixes
- `[Refactor]` - Code restructuring
- `[Docs]` - Documentation updates
- `[Test]` - Test additions/updates
- `[Config]` - Configuration changes

### Best Practices
1. **Atomic Commits**
   - One logical change per commit
   - Keep commits small and focused

2. **Clear Messages**
   - Present tense ("Add feature" not "Added feature")
   - Imperative mood ("Move cursor" not "Moves cursor")
   - No period at the end of the summary line

3. **Reference Tasks**
   - Always include Task ID from BACKLOG.md
   - Mark AI-assisted changes

## Pull Request Process

1. **Preparation**
   - Update branch with latest develop
   - Run all tests
   - Update documentation
   - Check acceptance criteria

2. **PR Types and Descriptions**

   Each PR type has specific requirements and formats. Choose the appropriate type based on:
   - Feature PR: New functionality or significant changes
   - Backlog PR: Updates to project planning and tasks
   - Documentation PR: Updates to any documentation
   - Bug Fix PR: Corrections to existing functionality

   **Priority Handling**:
   1. Bug fixes affecting production (hotfix)
   2. Feature PRs with dependencies
   3. Documentation and backlog updates
   4. Other changes

   **PR Size Guidelines**:
   - Small (preferred): < 200 lines changed
   - Medium: 200-500 lines changed
   - Large: > 500 lines (consider splitting)
   - Maximum files changed: 10

   **Draft and WIP Guidelines**:
   - Use draft PRs for work in progress
   - Prefix commit messages with "WIP:" for incomplete changes
   - Move to ready when all checks pass
   - Draft PRs should still follow naming conventions

   **Review Process**:
   - Developer approval required for all changes
   - AI agents can suggest improvements
   - AI agents cannot approve PRs
   - All comments must be addressed before merge

   **Merge Conflict Resolution**:
   ```bash
   # Update your branch
   git fetch origin
   git checkout your-branch
   git rebase origin/main
   
   # If conflicts occur
   # 1. Fix conflicts in your editor
   # 2. Stage resolved files
   git add resolved-file.scala
   # 3. Continue rebase
   git rebase --continue
   # 4. Force push (only for PR branches)
   git push origin your-branch -f
   ```

   **Hotfix Process**:
   1. Create branch from main: `hotfix/critical-fix`
   2. Make minimal required changes
   3. Full test suite must pass
   4. Merge directly to main
   5. Tag release if needed

   a. **Feature PRs**
   ```bash
   # Branch naming: feature/P0-1-1-short-description
   git checkout -b feature/P0-1-1-add-streaming
   
   # Commit message
   git commit -m "[Feat] Add streaming response support" -m "- Implement streaming client
   - Add progress indicators
   - Handle partial responses
   
   Task: #P0-1-1
   AI: Yes"

   # Create PR
   gh pr create --title "[Feat] Short description" --body "## Description
   Brief overview of the feature.

   ## Changes
   - List specific implementation changes
   - Include architectural decisions
   - Note any dependency updates

   ## Testing
   - [ ] Unit tests added/updated
   - [ ] Integration tests added/updated
   - [ ] Manual testing completed

   ## Documentation
   - [ ] Code comments updated
   - [ ] README updated if needed
   - [ ] API docs updated if needed

   ## Size
   - [ ] Changes are appropriately scoped
   - [ ] Large changes are split into smaller PRs
   - [ ] Number of files changed: [X]

   Task: #P0-1-1
   AI: Yes/No"
   ```

   b. **Backlog Updates**
   ```bash
   # Branch naming: docs/backlog-update-description
   git checkout -b docs/backlog-update-add-metrics

   # Commit message
   git commit -m "[Docs] Update backlog with metrics tasks" -m "- Add P3 metrics collection tasks
   - Update task priorities
   - Add technical notes
   
   Task: Backlog Update
   AI: Yes"

   # Create PR
   gh pr create --title "[Docs] Update backlog" --body "## Description
   Brief overview of backlog changes.

   ## Changes
   - List specific changes to BACKLOG.md
   - Note any related doc updates

   ## Validation
   - [ ] Follows backlog format
   - [ ] Items properly categorized
   - [ ] Clear acceptance criteria
   - [ ] Technical notes included
   - [ ] Effort estimated

   Task: Backlog Update
   AI: Yes/No"
   ```

   c. **Documentation Updates**
   ```bash
   # Branch naming: docs/update-type-description
   git checkout -b docs/update-api-docs

   # Commit message
   git commit -m "[Docs] Update API documentation" -m "- Add streaming API docs
   - Update quick start guide
   - Add usage examples
   
   Task: Documentation
   AI: Yes"

   # Create PR
   gh pr create --title "[Docs] Update documentation" --body "## Description
   Brief overview of documentation changes.

   ## Changes
   - List updated documents
   - Note significant changes

   ## Validation
   - [ ] Technical accuracy verified
   - [ ] Grammar and style checked
   - [ ] Links verified
   - [ ] Examples tested

   Task: Documentation
   AI: Yes/No"
   ```

   d. **Bug Fixes**
   ```bash
   # Branch naming: bugfix/short-description or hotfix/short-description
   git checkout -b bugfix/fix-connection-timeout

   # Commit message
   git commit -m "[Fix] Fix connection timeout issues" -m "- Increase retry delay
   - Add connection pooling
   - Improve error messages
   
   Task: #P0-1-1
   AI: Yes"

   # Create PR
   gh pr create --title "[Fix] Short description" --body "## Description
   Brief overview of the bug and fix.

   ## Problem
   - Describe the bug
   - Note impact and scope

   ## Solution
   - Explain the fix
   - Note any limitations

   ## Testing
   - [ ] Regression tests added
   - [ ] Fix verified
   - [ ] No new issues introduced

   Task: #P0-1-1
   AI: Yes/No"
   ```

   **Required Reviewers**:
   - Code changes: At least one senior developer
   - Documentation: Technical writer or maintainer
   - Backlog: Project manager or tech lead
   - Critical fixes: Two senior developers

3. **Review Process**
   - At least one approval required
   - All comments addressed
   - All tests passing
   - Documentation updated

## Release Process

1. **Prepare Release**
   - Create release branch
   - Update version numbers
   - Update CHANGELOG.md

2. **Release Branch Format**
   - `release/v1.0.0`
   - Follow semantic versioning

3. **Post-Release**
   - Merge to main
   - Tag release
   - Update develop
   - Clean up branches

## Security Guidelines

### Sensitive Data
- Never commit secrets or credentials
- Use environment variables for sensitive data
- Configure git-secrets for automated scanning
- Use .gitignore for sensitive files
- Regular audit of git history

### Branch Protection Rules
1. **Main Branch**
   - Require pull request reviews
   - Require status checks to pass
   - Require signed commits
   - No force push allowed
   - Include administrators

2. **Develop Branch**
   - Require pull request reviews
   - Require status checks to pass
   - Allow force push with lease
   - Protected from deletion

### Git Hooks
1. **Pre-commit Hooks**
   ```bash
   # .git/hooks/pre-commit
   - Scala formatting check
   - Unit tests
   - Security scanning
   - TODO/FIXME check
   ```

2. **Pre-push Hooks**
   ```bash
   # .git/hooks/pre-push
   - Integration tests
   - Performance benchmarks
   - Coverage check
   ```

3. **Commit-msg Hooks**
   ```bash
   # .git/hooks/commit-msg
   - Format validation
   - Task ID verification
   - AI assistance marking
   ```

## Changelog Guidelines

### Format
```markdown
# Changelog
All notable changes to this project will be documented in this file.

## [Unreleased]
### Added
- New features

### Changed
- Changes in existing functionality

### Deprecated
- Soon-to-be removed features

### Removed
- Removed features

### Fixed
- Bug fixes

### Security
- Security fixes
```

### Rules
1. Keep a changelog entry for every user-facing change
2. Use semantic versioning
3. Include migration notes when needed
4. Link to relevant issues/PRs
5. Date each release

## Git Commands Quick Reference

```bash
# Start new feature
git checkout develop
git checkout -b feature/P0-1-1-remove-client-layer

# Commit changes
git add .
git commit -m "[Feat] Remove redundant client layer

- Remove Client.default from Main
- Update service composition

Task: #P0-1-1
AI: Yes"

# Update feature branch
git checkout feature/P0-1-1-remove-client-layer
git fetch origin
git rebase origin/develop

# Create PR
git push origin feature/P0-1-1-remove-client-layer
```

## Cherry-pick Guidelines
```bash
# Cherry-pick a commit to another branch
git checkout target-branch
git cherry-pick commit-hash

# If conflicts occur
git add resolved-files
git cherry-pick --continue

# Push changes
git push origin target-branch
```

## PR Update Process
```bash
# Update PR with latest main
git checkout your-branch
git fetch origin
git rebase origin/main
git push origin your-branch -f

# Update PR with fixup commits
git commit --fixup=original-commit-hash
git rebase -i --autosquash original-commit-hash^
git push origin your-branch -f