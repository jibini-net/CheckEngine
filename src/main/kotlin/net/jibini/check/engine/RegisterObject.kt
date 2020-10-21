package net.jibini.check.engine

/**
 * Indicates that the annotated class should be detected by the classpath scanner and automatically added to the list of
 * managed engine objects
 *
 * @author Zach Goethel
 */
@Target(AnnotationTarget.CLASS)
@Retention
annotation class RegisterObject