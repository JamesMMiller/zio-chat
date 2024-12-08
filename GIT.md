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

2. **PR Template**
   ```markdown
   ## Description
   Brief description of changes

   ## Task Reference
   - Task: #P0-1-1
   - AI Assisted: Yes/No

   ## Acceptance Criteria
   - [ ] Criterion 1
   - [ ] Criterion 2

   ## Testing
   - [ ] Unit tests added/updated
   - [ ] Integration tests added/updated
   - [ ] Manual testing completed

   ## Documentation
   - [ ] Code comments updated
   - [ ] README updated
   - [ ] BACKLOG.md status updated
   ```

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