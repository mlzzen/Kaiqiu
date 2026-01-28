# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android mobile application built with Kotlin and Jetpack Compose. The app follows standard Android project structure with a single module (`app/`).

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumentation tests
./gradlew lint                   # Run lint checks
./gradlew build                  # Full build with tests
./gradlew clean                  # Clean build artifacts
```

## Architecture

- **Package:** `dev.mlzzen.kaiqiu`
- **UI Framework:** Jetpack Compose with Material 3
- **Navigation:** Adaptive `NavigationSuiteScaffold` (Home, Favorites, Profile)
- **Theming:** Custom Material 3 theme in `app/src/main/java/dev/mlzzen/kaiqiu/ui/theme/`

## Key Dependencies

- AndroidX Core KTX, Lifecycle Runtime, Activity Compose
- Compose BOM 2024.09.00
- Material 3
- JUnit 4 (unit tests), Espresso Core (instrumentation tests)

## SDK Configuration

- Compile/Target SDK: 36
- Min SDK: 30
- Java: 11
- Gradle: Kotlin DSL with version catalog (`gradle/libs.versions.toml`)

## Testing

- Unit tests: `app/src/test/` (JUnit, runs on JVM)
- Instrumentation tests: `app/src/androidTest/` (runs on device/emulator)

## Shell

- **Default Shell:** Nushell (nu)
- Nushell does not support the `&&` operator for chaining commands. When running multiple commands sequentially, use semicolons (`;`) or run them separately instead.
