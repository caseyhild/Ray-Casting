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

<img width="420" alt="Screenshot 2026-05-04 at 11 45 34 PM" src="https://github.com/user-attachments/assets/f0df6f95-c0f5-4c99-afeb-b1b25ee399c6" />
<img width="420" alt="Screenshot 2026-05-04 at 11 45 12 PM" src="https://github.com/user-attachments/assets/a2c10f06-b6a8-4321-9a55-375374102c6d" />
<img width="420" alt="Screenshot 2026-05-04 at 11 42 18 PM" src="https://github.com/user-attachments/assets/530b7f87-60a2-4eff-a515-1257264cf2a0" />
<img width="420" alt="Screenshot 2026-05-04 at 11 43 10 PM" src="https://github.com/user-attachments/assets/1468f3d0-b06f-4949-8580-a7f9c5afb263" />
