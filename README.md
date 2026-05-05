# Ray Casting Engine

A 3D ray casting engine built in Java with multi-level environments, vertical camera movement, and point cloud sprites. Features parallel rendering for high performance.

## Features

- **Multi-level worlds** - Two-story environments with jumping mechanics
- **Mouse look** - Full 3D camera with horizontal and vertical rotation
- **Parallel rendering** - Multi-threaded rendering pipeline for smooth performance
- **Distance fog** - Atmospheric depth effects
- **Point cloud sprites** - Complex structures rendered in the world from 3D point data
- **Shooting mechanics** - Projectile system with depth testing

## Controls

- **WASD** - Movement
- **Mouse** - Look around
- **Space** - Jump
- **Left Click** - Shoot

## Running

### From Source
```bash
cd src/RayCasting
javac RayCasting.java
java RayCasting
```

### From JAR Files

**Ray Casting Game:**
```bash
java -jar RayCasting.jar
```
Or simply double-click `RayCasting.jar`

**Texture Editor:**
```bash
java -jar TextureEditor.jar
```
Or simply double-click `TextureEditor.jar`

The JAR files are self-contained and will automatically extract required resources to a `resources/` folder on first run.

### From macOS App Bundles

**Ray Casting Game:**
Double-click `RayCasting.app` or run:
```bash
open RayCasting.app
```

**Texture Editor:**
Double-click `TextureEditor.app` or run:
```bash
open TextureEditor.app
```

The .app bundles are fully self-contained and include:
- Embedded Java runtime (no Java installation required)
- Application JAR file
- All resources (textures, 3D point data)
- Native macOS launcher

**Note:** The .app bundles are larger (~250-500 MB) because they include a complete Java runtime, but users can run them without any dependencies.

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
1. Select a color using the gradient picker or type RGB values
2. Choose a brush size or drawing mode
3. Click or drag on the 64x64 grid to draw
4. Click SAVE to export your texture

## Project Structure

```
Ray-Casting/
├── RayCasting.jar           # Executable JAR for the game
├── TextureEditor.jar        # Executable JAR for texture editor
├── RayCasting.app/          # macOS app bundle for the game
├── TextureEditor.app/       # macOS app bundle for texture editor
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
    └── Vector3D.java        # 3D vector math
```

## Screenshots

<img width="410" alt="Screenshot 2026-05-04 at 11 45 34 PM" src="https://github.com/user-attachments/assets/f0df6f95-c0f5-4c99-afeb-b1b25ee399c6" />
<img width="410" alt="Screenshot 2026-05-04 at 11 45 12 PM" src="https://github.com/user-attachments/assets/a2c10f06-b6a8-4321-9a55-375374102c6d" />
<img width="410" alt="Screenshot 2026-05-04 at 11 42 18 PM" src="https://github.com/user-attachments/assets/530b7f87-60a2-4eff-a515-1257264cf2a0" />
<img width="410" alt="Screenshot 2026-05-04 at 11 43 10 PM" src="https://github.com/user-attachments/assets/1468f3d0-b06f-4949-8580-a7f9c5afb263" />
