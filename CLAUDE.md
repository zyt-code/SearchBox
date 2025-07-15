# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SearchBox is an Android App Widget that provides desktop search functionality for installed apps. Users can add the widget to their home screen, tap it to open a search interface, and quickly find and launch any installed application.

## Architecture

### Core Components
- **SearchWidgetProvider**: App widget provider that handles widget updates and click events
- **SearchActivity**: Main search interface with real-time app filtering
- **AppAdapter**: RecyclerView adapter for displaying app list with icons and names
- **AppInfo**: Data model representing an installed application

### Key Features
- Desktop widget with search box appearance
- Real-time fuzzy search through installed apps
- App icon and name display in search results
- One-tap app launching
- Empty state handling for no results

## Development Commands

This is a standard Android Gradle project. Common commands:

```bash
# Build the project
./gradlew build

# Install debug APK
./gradlew installDebug

# Run tests
./gradlew test

# Clean build
./gradlew clean
```

## Important Implementation Details

### Permissions
- `QUERY_ALL_PACKAGES` permission is required for Android 11+ to query all installed apps
- Widget requires home screen launcher support

### Android Compatibility
- Minimum SDK: Android 10 (API 29)
- Target SDK: Android 14 (API 34)
- Uses Material Design 3 components

### Package Structure
```
com.searchbox/
├── SearchWidgetProvider.java    # Widget provider
├── SearchActivity.java          # Main search UI
├── AppAdapter.java             # RecyclerView adapter  
└── AppInfo.java                # App data model
```

### Key Technical Notes
- Widget layout uses rounded corners and Material Design styling
- Search is case-insensitive and matches both app names and package names
- Apps are sorted alphabetically for consistent display
- Only launchable apps (those with launcher intents) are included in search results