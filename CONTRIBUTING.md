# Contributing to Lumina AI

Thank you for your interest in contributing to Lumina AI! This document provides guidelines and instructions for contributing to the project.

---

## 🎯 Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help others learn and grow
- Report issues responsibly

---

## 📋 Development Setup

### Prerequisites
- Android Studio 2024.1 or later
- JDK 11+
- Gradle 9.7.1+
- Git

### Clone & Setup
```bash
git clone https://github.com/theflaxyplays-dotcom/lumina-online-.git
cd lumina-online-
./gradlew build
```

### Environment Configuration
```bash
# Copy example env file
cp .env.example .env

# Add your API keys
# GEMINI_API_KEY=your_key_here
# GROQ_API_KEY=your_key_here
```

---

## 🐛 Reporting Issues

### Before Creating an Issue
1. Search existing issues to avoid duplicates
2. Check the README and ARCHITECTURE.md
3. Verify with latest code on `main` branch

### Issue Template
```markdown
## Description
Clear description of the issue

## Steps to Reproduce
1. Step 1
2. Step 2
3. Step 3

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- Android Version: [e.g., 13]
- Device: [e.g., Pixel 6]
- App Version: [e.g., 1.0]
```

---

## ✨ Feature Requests

1. **Clear Title**: Briefly describe the feature
2. **Detailed Description**: Explain the use case and benefits
3. **Implementation Ideas**: Suggest how it might work
4. **Alternative Solutions**: Consider other approaches

---

## 🔄 Pull Request Process

### Before You Start
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/descriptive-name`
3. Stay updated with main: `git fetch origin && git rebase origin/main`

### Making Changes
```bash
# Work on your feature
git add .
git commit -m "feat: description of changes"

# Push to your fork
git push origin feature/descriptive-name
```

### Code Quality Standards

#### Kotlin Style
```kotlin
// ✅ Good: Clear naming, proper formatting
fun processUserMessage(message: String): String {
    return message.trim().lowercase()
}

// ❌ Bad: Unclear naming, poor formatting
fun f(m: String) = m.trim().lowercase()
```

#### Compose Best Practices
```kotlin
// ✅ Good: Modular, reusable components
@Composable
fun ChatMessage(text: String, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier)
}

// ❌ Bad: Monolithic, hard-coded
@Composable
fun ChatMessageBig() {
    Text(text = "hardcoded", modifier = Modifier.fillMaxWidth())
}
```

#### Comments & Documentation
```kotlin
/**
 * Processes voice input and returns parsed command.
 * 
 * @param audioData Raw audio bytes from microphone
 * @return Parsed command string or null if unrecognized
 * @throws IllegalArgumentException if audioData is empty
 */
fun parseVoiceCommand(audioData: ByteArray): String?
```

### Testing Requirements

- **Unit Tests**: Test business logic
- **Compose UI Tests**: Test UI interactions
- **Screenshot Tests**: Use Roborazzi for UI snapshots

```kotlin
// Example test
@Test
fun testChatMessageDisplay() {
    composeTestRule.setContent {
        ChatMessage(text = "Hello!")
    }
    composeTestRule.onNodeWithText("Hello!").assertIsDisplayed()
}
```

### Commit Message Format

```
<type>: <subject>

<body>

<footer>
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

**Examples**:
```
feat: add voice command parsing for AutomationScreen

- Implemented Google Speech API integration
- Added voice activity detection
- Handles multiple accent variations

Fixes #123
```

### PR Description Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Related Issues
Fixes #(issue number)

## Testing
Describe how to test these changes

## Checklist
- [ ] Code follows style guidelines
- [ ] Added/updated tests
- [ ] Updated documentation
- [ ] No new warnings generated
```

---

## 📚 Architecture Guidelines

### Folder Structure
```
app/src/main/java/com/example/
├── ui/screens/          # Screen-level composables
├── ui/components/       # Reusable components
├── data/local/          # Room database entities
├── data/remote/         # API clients
├── service/             # Background services
└── ui/viewmodel/        # MVVM ViewModels
```

### State Management
- Use `StateFlow` for observable state
- Lift state to parent composables
- Keep ViewModels lean

```kotlin
// Good pattern
class LuminaViewModel : ViewModel() {
    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()
}
```

### Dependencies
- Add new dependencies to `libs.versions.toml`
- Review for security and maintenance
- Avoid unnecessary large libraries

---

## 📊 Performance Guidelines

- Minimize recompositions in Compose
- Use LazyColumn for long lists
- Avoid heavy computations on main thread
- Profile with Android Profiler

---

## 🔐 Security Considerations

- Never commit secrets (`.env` files)
- Use Android KeyStore for sensitive data
- Validate all user inputs
- Follow Android security best practices

---

## 📝 Documentation

Update docs when:
- Adding new features
- Changing API behavior
- Fixing security issues
- Improving architecture

Files to update:
- `README.md` - User-facing information
- `ARCHITECTURE.md` - Technical documentation
- Code comments - Complex logic
- Commit messages - Change history

---

## 🎓 Learning Resources

- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)
- [Android Architecture Components](https://developer.android.com/topic/architecture)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-overview.html)
- [Room Database Docs](https://developer.android.com/training/data-storage/room)

---

## ✅ Review Process

1. Automated checks (build, lint, tests)
2. Code review by maintainers
3. Address feedback and update PR
4. Approval and merge

---

## 🚀 Deployment

- Main branch is auto-deployed to GitHub Artifacts
- Releases include changelog and version bump
- PRs are validated before merging

---

## ❓ Questions?

- Create a Discussion on GitHub
- Ask in issues with `[QUESTION]` prefix
- Check existing documentation first

---

**Thank you for contributing to Lumina AI!** 🙏
