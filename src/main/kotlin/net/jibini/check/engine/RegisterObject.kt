package net.jibini.check.engine

/**
 * Indicates that the annotated class should be detected by the
 * classpath scanner and automatically added to the list of managed
 * engine objects.
 *
 * Annotated classes must have a zero-argument constructor. An instance
 * of the annotated class will be created upon the creation of the game
 * context. If the annotated class implements [Initializable], the
 * instance will be initialized upon game boot. If the annotated class
 * implements [Updatable], the instance will be updated each frame.
 *
 * Run setup code for an engine object in the initialize method. The
 * game context will not be ready and game object injection will not be
 * completed when the annotated instance is instantiated.
 *
 * @author Zach Goethel
 */
@Target(AnnotationTarget.CLASS)
@Retention
annotation class RegisterObject