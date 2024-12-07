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