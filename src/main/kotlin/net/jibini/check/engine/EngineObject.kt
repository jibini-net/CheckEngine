package net.jibini.check.engine

/**
 * Indicates that the annotated field is an injection point for engine
 * object instances on the current thread.
 *
 * @author Zach Goethel
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention
annotation class EngineObject