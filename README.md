# Check Engine <img width="20px" src="https://i.imgur.com/zCbt199.png" />

A simple 2D game engine developed for the Makeshift group at University of Wisconsin-La Crosse.

This engine is designed to be extremely simple to use, quick for prototyping, and friendly to beginners.  Animation should be as simple as loading in GIFs; geometry rendering should be as simple as specifying rectangle coordinates and size.  The core engine is written in Kotlin and is interoperable with Java during game development.  The graphical systems are built on LWJGL 3.2.3 and do not rely on any bloated 3rd-party library dependencies.

-----

## Importing the project
This project requires the use of IntellIJ IDE for development.  Artifacts can also be built by running the command-line Gradle wrapper distributed with the engine source.

Import the repository into an IntelliJ workspace.  Attempt to run the `main` method in  `TestGame` from the test source folder.  It will likely fail to run.

Make the following changes to the generated run configuration (and any run configurations which are created in the future):
 * Open the `Edit Configurations...` dialog from the top right in the editing window
 * Add a `Before launch` Gradle task prior to `Build`
 * Add the following to the Gradle tasks: `kaptKotlin kaptTestKotlin`
 
These changes ensure that the compiler runs the annotation processor prior to building.