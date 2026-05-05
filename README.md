# Ray Casting Engine

A 3D ray casting engine built in Java with multi-level environments, vertical camera movement, and point cloud sprites. Features parallel rendering for high performance.

## Features

- **Multi-level worlds** - Two-story environments with jumping mechanics
- **Mouse look** - Full 3D camera with horizontal and vertical rotation
- **Parallel rendering** - Multi-threaded rendering pipeline for smooth performance
- **Distance fog** - Atmospheric depth effects
- **Point cloud sprites** - Trees and spirals rendered from 3D point data
- **Shooting mechanics** - Projectile system with depth testing
- **Procedural placement** - Trees spawn with wall collision avoidance

## Controls

- **WASD** - Movement
- **Mouse** - Look around
- **Space** - Jump
- **Left Click** - Shoot

## Running

```bash
cd src/RayCasting
javac RayCasting.java
java RayCasting
```

## Project Structure

```
src/RayCasting/
├── RayCasting.java      # Main game loop and initialization
├── Screen.java          # Rendering engine with parallel pipeline
├── Camera.java          # Input handling and player movement
├── Texture.java         # Texture generation and animation
├── Shot.java            # Projectile physics
├── Tree.java            # Tree sprite generator
├── Spiral.java          # Spiral sprite generator
├── CreatePoints.java    # 3D point file creation
├── PointsFile.java      # Point cloud data structure
├── TextureCreator.java  # Texture editing tool
├── Vector2D.java        # 2D vector math
└── Vector3D.java        # 3D vector math
```

## Screenshots

<img width="410" alt="Multi-level environment with trees" src="https://github.com/user-attachments/assets/0e365ffb-4785-40de-8ef2-f33dfa213d62" />
<img width="410" alt="Looking up at upper level" src="https://github.com/user-attachments/assets/a154e8c5-19e6-4734-bc93-2698ff4c79d4" />
<img width="410" alt="Distance fog and sprites" src="https://github.com/user-attachments/assets/f54fd53c-4208-428b-9a88-1c46ea7f0f22" />
<img width="410" alt="Complex geometry rendering" src="https://github.com/user-attachments/assets/4f2dae93-8169-472b-8b48-75b7f97befb3" />
