
# MapArt - Fabric 1.21.4 Mod

this entire project is being made with codex

A Minecraft Fabric mod for Minecraft 1.21.4 designed to automate the process of making mapart. Currently this release will not be updated as id prefer to code it myself and not have to deal with issues with a very minimal release which is what this is. **I will provide help and if you find fatal errors/crashes that prevent usage of this or you have a suggestion/recommendation, then feel free to make an issue or tell me on discord @bstarr otherwise no further public updates will be made so keep that in mind** 

In the meantime im going to continue developing this on my own until im satisfied its reached my goals or atleast works enough to be worth using over other methods/manual mapart making. This mod requires no major setup which is the main goal of this to just be a portable mapart bot - recommended to use on servers with a dupe, however carpets work fine since most servers allow carpet duplication.

## Development Setup

### Prerequisites
- JDK 21 or higher 
- Git

### Getting Started

1. **Clone and navigate to the project:**
   ```bash
   git clone <your-repo-url>
   cd <project-folder>
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
1. Import project
2. Wait for gradle to import
3. Press the build button in gradle
4. Compiled jar in `build/libs/` **don't pick the sources .jar**

## In-Game Commands
VVVVVVVVVV
- `/mapart` 

Available subcommands ( theres more but ill explain these because some are straightforward ) :
- `load <path>` e.g `/mapart load bedrock.nbt` ( place the schematic in the games folder for easier typing)
- `info` - will print the loaded schematics info useful for materials - will show total + X stacks + X blocks for each item
- `unload` - basically reset
- `panic` (client-side emergency stop + unload - Can be binded in keybinds )
- `clienttimerspeed <multiplier>` (sets how many assisted automation passes run each client tick; useful for testing - wouldn't use online unless server allows timer)
- `supply` `add <name>` - will prompt you to right click a container to set a supply point - `clear` clears the list but kinda broken haven't looked further into the issue but does work? `list` - shows supplys with coords and a number/id `remove <id>` add the id to remove it from the list
- `setorigin` you cannot start without an origin point so stand where you want it and issue the command or alternatively put the coords in after e.g `/mapart setorigin x y z`
- `settings` - will list all the settings and their values `set <setting> <value>` some are boolean, some are values so check beforehand - note `overlaycurrentregiononly false` will render the entire overlay rather then the current chunk/region


If `/mapart` is unknown in game:
- make sure the built JAR from `build/libs/` is in your server/client `mods/` folder
- confirm Fabric API is also installed on the same instance
- run `/help mapart` to verify command registration


## Baritone Integration

MapArtRunner integrates with [Baritone](https://github.com/cabaletta/baritone) through `baritone-api-fabric` at runtime.

- Install a Baritone jar that exposes the `baritone-api-fabric` API for your exact Minecraft version (1.21.4 in this project).
- If Baritone is missing or the API is mismatched, `/mapart start` now reports a friendly in-game error and assisted movement is disabled until a compatible Baritone jar is installed.

## Mod Details

- **Minecraft Version:** 1.21.4
- **Fabric Loader:** 0.16.5
- **Fabric API:** 0.104.0+1.21.1
- **Java Version:** 21

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
