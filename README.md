# Ray Casting Engine

A 3D ray casting engine built in Java with multi-level environments, vertical camera movement, and point cloud sprites. Features parallel rendering for high performance.

## Features

- **Multi-level worlds** - Two-story environments with jumping mechanics
- **Mouse look** - Full 3D camera with horizontal and vertical rotation
- **Parallel rendering** - Multi-threaded rendering pipeline for smooth performance
- **Distance fog** - Atmospheric depth effects
- **Point cloud sprites** - Complex structures rendered in the world from 3D point data
- **Shooting mechanics** - Projectile system with depth testing
- **Texture Editor** - Built-in tool for creating custom 64x64 textures

## Controls

- **WASD** - Movement
- **Mouse** - Look around
- **Space** - Jump
- **Left Click** - Shoot
- **Escape** - Quit

## Running

### Option 1: JAR Files (Recommended)

**Ray Casting Game:**
```bash
java -jar RayCasting.jar
```

**Texture Editor:**
```bash
java -jar TextureEditor.jar
```

✅ **Advantages:**
- Works immediately, no setup required
- No special permissions needed
- Smaller file size (~63 KB each)

⚠️ **Requirements:**
- Java 11 or later must be installed

### Option 2: macOS App Bundles

**Ray Casting Game:**
```bash
open RayCasting.app
```
Or double-click `RayCasting.app` in Finder

**Texture Editor:**
```bash
open TextureEditor.app
```
Or double-click `TextureEditor.app` in Finder

✅ **Advantages:**
- No Java installation required (includes embedded runtime)
- Native macOS integration with custom icons
- Can be placed in Applications folder

⚠️ **First-Time Setup Required:**

The app needs **Accessibility permissions** to control the mouse cursor for smooth camera movement. Without this, the cursor will get stuck at the edge of the screen.

**To grant permissions:**

1. Open **System Settings** (or **System Preferences** on older macOS)
2. Go to **Privacy & Security** → **Accessibility**
3. Click the **+** button (you may need to click the 🔒 lock icon first)
4. Navigate to and select the `RayCasting.app` you downloaded
5. Make sure the checkbox/toggle is **enabled**
6. **Restart the app**

**Note:** This is a one-time setup. The permission allows the app to keep the cursor centered for continuous mouse look.

**Troubleshooting:**
- If the cursor still gets stuck, make sure you fully quit (Cmd+Q) and restart the app
- If you see a security warning, right-click the app → **Open** (first time only)
- Alternatively, use the JAR file which doesn't require permissions

### Option 3: From Source

```bash
cd src/RayCasting
javac *.java
java RayCasting
```

## Building from Source

Use the included build script:
```bash
./build.sh
```

This will:
1. Compile all Java source files
2. Create JAR files with embedded resources
3. Generate macOS app bundles with custom icons
4. Apply necessary entitlements for accessibility permissions

## Texture Editor

The Texture Editor allows you to create and modify 64x64 textures for use in the game.

### Features
- **Color Picker** - Full RGB color selection with gradient picker
- **Manual RGB Input** - Type exact RGB values (0-255)
- **Brush Sizes** - 1x1, 3x3, 5x5, 7x7, 9x9, and Fill All
- **Drawing Modes**:
  - **Draw** - Paint with selected color and brush size
  - **Select Color** - Pick colors from existing pixels
  - **Copy** - Select and copy rectangular regions
  - **Paste** - Paste copied regions
- **Recently Used Colors** - Quick access to your last 5 colors
- **Template Library** - Load from 16 preset textures
- **Save/Load** - Save custom textures to `resources/SavedTextures/`

### Usage
1. Launch `TextureEditor.jar` or `TextureEditor.app`
2. Select a color using the gradient picker or type RGB values
3. Choose a brush size or drawing mode
4. Click or drag on the 64x64 grid to draw
5. Click **SAVE** to export your texture

Saved textures can be loaded in the main game by modifying the texture assignments in the code.

## Technical Details

### Resource Loading
- All resources (textures, 3D point data) are embedded in the JAR files
- Resources are read directly from the JAR using `getResourceAsStream()` - no extraction to disk
- Works from any location without requiring the original project directory

### Rendering
- Multi-threaded parallel rendering pipeline for optimal performance
- Ray casting algorithm with distance fog and lighting effects
- Point cloud sprites for complex 3D objects

### macOS Integration
- Custom .icns icons for both applications
- Embedded Java runtime (OpenJDK)
- Entitlements configured for accessibility permissions

## Project Structure

```
Ray-Casting/
├── RayCasting.jar           # Executable JAR for the game (~63 KB)
├── TextureEditor.jar        # Executable JAR for texture editor (~63 KB)
├── RayCasting.app/          # macOS app bundle for the game
├── TextureEditor.app/       # macOS app bundle for texture editor
├── RayCasting.icns          # Custom icon for game
├── TextureEditor.icns       # Custom icon for texture editor
├── build.sh                 # Build script for creating JARs and apps
├── entitlements.plist       # macOS entitlements for accessibility
├── resources/
│   ├── 3DPoints/
│   │   ├── tree.txt         # Tree point cloud data
│   │   └── spiral.txt       # Spiral point cloud data
│   └── SavedTextures/
│       ├── bricks.txt       # Brick texture
│       ├── dog.txt          # Dog texture
│       ├── fractal.txt      # Fractal texture
│       ├── map.txt          # Map texture
│       ├── tiles.txt        # Tiles texture
│       └── waves.txt        # Waves texture
└── src/RayCasting/
    ├── RayCasting.java      # Main game loop and initialization
    ├── Screen.java          # Rendering engine with parallel pipeline
    ├── Camera.java          # Input handling and player movement
    ├── Texture.java         # Texture generation and animation
    ├── Shot.java            # Projectile physics
    ├── Zombie.java          # Enemy entity
    ├── Tree.java            # Tree sprite generator
    ├── Spiral.java          # Spiral sprite generator
    ├── CreatePoints.java    # 3D point file creation
    ├── PointsFile.java      # Point cloud data structure
    ├── TextureEditor.java   # Texture editing tool
    ├── Vector2D.java        # 2D vector math
    ├── Vector3D.java        # 3D vector math
    ├── manifest-raycasting.txt      # JAR manifest for game
    └── manifest-textureeditor.txt   # JAR manifest for editor
```

## Additional Documentation

- **QUICK_START.md** - Quick reference guide
- **CURSOR_FIX_INSTRUCTIONS.md** - Detailed permission setup guide
- **BUILD_SUMMARY.md** - Technical details about the build process
- **FINAL_BUILD_SUMMARY.md** - Complete overview of fixes and improvements

## Screenshots

<img width="410" alt="Screenshot 2026-05-04 at 11 45 34 PM" src="https://github.com/user-attachments/assets/f0df6f95-c0f5-4c99-afeb-b1b25ee399c6" />
<img width="410" alt="Screenshot 2026-05-04 at 11 45 12 PM" src="https://github.com/user-attachments/assets/a2c10f06-b6a8-4321-9a55-375374102c6d" />
<img width="410" alt="Screenshot 2026-05-04 at 11 42 18 PM" src="https://github.com/user-attachments/assets/530b7f87-60a2-4eff-a515-1257264cf2a0" />
<img width="410" alt="Screenshot 2026-05-04 at 11 43 10 PM" src="https://github.com/user-attachments/assets/1468f3d0-b06f-4949-8580-a7f9c5afb263" />
