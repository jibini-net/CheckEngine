package net.jibini.check.engine

/**
 * Indicates that the annotated field should be filled with the current thread's engine object instance
 *
 * @author Zach Goethel
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention
annotation class EngineObject