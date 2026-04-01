# FallingPipeRender_Java

Java floor renderer scaffold using LibGDX. This project is set up so you can:

1. Run immediately with a fallback textured floor.
2. Drop in a Blender-exported floor model later and render it without changing code.

## Current behavior

- If `Assets/Models/floor.obj` exists, the app loads and renders it.
- If it does not exist yet, the app renders a fallback floor using `Assets/Textures/Tile-texture.jpg`.

## Project structure

- `src/main/java/com/pipefalling/DesktopLauncher.java`
- `src/main/java/com/pipefalling/FloorRendererApp.java`
- `Assets/Textures/`
- `Assets/Models/` (create this when you export from Blender)

## Run

From the `FallingPipeRender_Java` directory (recommended, no global Gradle required):

```bash
gradlew.bat run
```

Alternative commands:

```bash
./gradlew run
gradle run
```

## Blender export for floor

1. In Blender, select the floor object.
2. `File -> Export -> Wavefront (.obj)`
3. Use these key settings:
	- Selection Only: On
	- Apply Modifiers: On
	- Include UVs: On
	- Write Materials: On
	- Triangulate Faces: On (optional but recommended)
4. Save as `floor.obj` (and `floor.mtl`) into `Assets/Models/`.

Recommended result:

- `Assets/Models/floor.obj`
- `Assets/Models/floor.mtl`
- texture files referenced by `floor.mtl` placed in `Assets/Models/` or `Assets/Textures/`

## Camera controls

- Left mouse drag: orbit
- Scroll: zoom

## Next step

After the floor is working, we can add the pipe/table models and animate the pipe fall in the same render loop.
