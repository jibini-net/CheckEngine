# Check Engine <img width="20px" src="https://i.imgur.com/zCbt199.png" />

A simple 2D game engine developed for the Makeshift group at University of 
Wisconsin-La Crosse.

This engine is designed to be extremely simple to use, quick for prototyping,
and friendly to beginners.  Animation should be as simple as loading in GIFs;
geometry rendering should be as simple as specifying rectangle coordinates and 
size.  The core engine is written in Kotlin and is interoperable with Java 
during game development.  The graphical systems are built on LWJGL 3.2.3 and 
do not rely on any bloated 3rd-party library dependencies.

It was developed as a passion project/hobbiest endeavor.  Contributions would
be appreciated, but the end result isn't expected to be amazing.  We're looking
for a student-made game in a student-made arcade cabinet.  We're doing this
purely for fun.  Enjoy!

-----

## Importing the project
This project requires the use of a Kotlin-capable IDE for development.  I
recommend [IntelliJ Community](https://www.jetbrains.com/idea/download/#section=windows),
which has excellent Kotlin integration.  Importing this repository as a Gradle
project should download all the necessary libraries and dependencies to build
and run.

A rudimentary test game is located in the class `net.jibini.check.TestGame`
which demonstrates lighting, physics, UI, animation, and other key engine features.

## System requirements
The graphical backend of this game requires modern OpenGL support, but relatively
low graphical horsepower.  Check Engine is compatible with Windows on X86/64
and Linux on ARM32/64.  It is actively tested on the Raspberry Pi 4 and can use
either OpenGL 4.3 Core or OpenGL ES 3.0.

 * 4-8 GB system memory
 * 512 MB or more VRAM
 * OpenGL 4.3 Core or OpenGL ES 3.0
 * JDK or OpenJDK 8 or higher
 * Windows on `x86`/`x86_64`
 * or Linux on `armhf`/`aarch64`

-----

## Features

### Singleton dependency injection
The engine maintains a set of singleton instances of several important game
elements.  Any class annotated with `@RegisterObject` will automatically be
created and maintained over the lifecycle of the game.  Several game components
can be initialized on game boot, updated during the runtime of the game, or
called on specific events during gameplay.

Check out this [wiki page](https://github.com/zgoethel/CheckEngine/wiki/@EngineObject-Singletons)
for more details and instructions.  Those familiar with Spring Boot's `@Autowired`
and `@Component` stereotypes will feel right at home.

### Cross-platform support for OpenGL and OpenGL ES
Rendering in this engine is either performed via OpenGL 4.3 Core or OpenGL ES 3.0.
To switch the engine to OpenGL ES mode, create a file named `opengl_es` in the
project root directory.  All natives distributed with this engine, including
the custom build of [ImGui](https://github.com/SpaiR/imgui-java), are supported
on `armhf` and `aarch64`.

### Integration with ImGui
Pre-packaged functionality includes [ImGui](https://github.com/ocornut/imgui),
specifically a custom build of the [Java bindings](https://github.com/SpaiR/imgui-java).
Game developers who use this engine can quickly and easily add UI to their
games.

This dialog UI was created with very few lines of code.

![](https://raw.githubusercontent.com/zgoethel/CheckEngine/kt/photos/imgui_integration.png)

### Easy character and tile animations
On-screen animations, such as character walk cycles and attacks, can be stored
as GIFs and loaded just like any other photo.  Animated GIFs are automatically
converted into sprite sheets by the engine, which also renders the animations as
if they were any regular texture.

```java
// Load an animated texture in one line of code.
Texture animated = Texture.load(Resource.fromClasspath("character/attack.gif"));
```

### Real-time raytraced lighting
Raytracing is a marketing buzzword, I know.

Multiple lights can be placed anywhere in a level, providing an atmospheric and
pleasant feel.  The lighting algorithm traces a collection of vectors eminating from
a lighting source, storing their length in a texture for later reference.  This is 
similar to a depth texture used in 3D shadows.  The shader finds the distance between 
each pixel on screen and the light, then compares that distance to the stored rays in
the raytraced depth texture.

The results vary, but here are a couple of examples:

![](https://raw.githubusercontent.com/zgoethel/CheckEngine/kt/photos/raytraced_lighting.png)

### 2D platforming and top-down physics
The game engine provides AABB collision detection and resolution between entities
and static game elements.  The physics engine also accounts for vehicle constructs,
such as a player travelling on a platform; platforms keep players on them and provide
velocity if they jump off.

It seems simple, but there's a lot going on under the hood.  Entities in the game
world are stored in a constantly-maintained quad tree.  The number of collision checks
in the world are greatly reduced by placing entities into a tree of buckets.

![](https://raw.githubusercontent.com/zgoethel/CheckEngine/kt/photos/platformer_physics.png)
