# MapArt - Fabric 1.21.1 Mod

A Minecraft Fabric mod for Minecraft 1.21.1.

## Development Setup

### Prerequisites
- JDK 21 or higher
- Git

### Getting Started

1. **Clone and navigate to the project:**
   ```bash
   cd MapArtrunner
   ```

2. **Generate the Minecraft development environment:**
   ```bash
   ./gradlew genSources
   ```
   (On Windows, use `gradlew.bat genSources`)

3. **Build the mod:**
   ```bash
   ./gradlew build
   ```

The compiled mod JAR will be located in `build/libs/`.

### IDE Setup

#### IntelliJ IDEA
1. Open the project in IntelliJ
2. Run `./gradlew idea` to generate IDE configurations
3. Reload the project

#### Eclipse
1. Run `./gradlew eclipse` to generate IDE configurations
2. Import the project as an existing project in Eclipse

#### VS Code
1. Install the "Extension Pack for Java" extension
2. Run `./gradlew build` to generate necessary files
3. Open the project in VS Code

### Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/mapart/
│   │       └── MapArtMod.java          # Main mod entry point
│   └── resources/
│       ├── fabric.mod.json              # Mod metadata
│       ├── mapart.mixins.json           # Mixin configuration
│       └── assets/mapart/               # Game assets (textures, sounds, etc.)
build.gradle                             # Gradle build configuration
gradle.properties                        # Project properties and versions
settings.gradle                          # Gradle settings
```

## Building and Running

**Build the mod:**
```bash
./gradlew build
```

**Development build:**
```bash
./gradlew genSources
./gradlew build
```

## Customization

Edit `gradle.properties` to customize:
- `mod_version` - Version of your mod
- `maven_group` - Package group (e.g., `com.yourname`)
- `archives_base_name` - JAR filename base

Edit `src/main/resources/fabric.mod.json` to customize:
- `name` - Display name of your mod
- `description` - Mod description
- `authors` - Your name
- `license` - License type
- `contact` - Links and contact info

## Mod Details

- **Minecraft Version:** 1.21.1
- **Fabric Loader:** 0.16.5
- **Fabric API:** 0.104.0+1.21.1
- **Java Version:** 21

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.