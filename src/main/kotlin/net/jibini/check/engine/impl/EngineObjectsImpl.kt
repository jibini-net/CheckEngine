package net.jibini.check.engine.impl

import io.github.classgraph.ClassGraph

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject

import org.slf4j.LoggerFactory

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible

/**
 * Handles the creation and injection of annotated engine objects into
 * annotated fields. Any class annotated with [RegisterObject] will be
 * created and injected into other engine objects.
 *
 * Any class which is either an engine object itself or [engine aware]
 * [EngineAware] will be injected with instances of engine objects.
 * Injection will be performed on engine objects at creation time, when
 * the context is created; injection will be performed on engine aware
 * objects at runtime when they are constructed.
 *
 * @author Zach Goethel
 */
object EngineObjectsImpl
{
    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * Collections of engine objects mapped to each thread. This is
     * a roundabout way of implementing thread-local engine objects;
     * equivalently, each instance is context-local.
     */
    private val threadObjects = ConcurrentHashMap<Thread, MutableList<Any>>()

    /**
     * The collection of engine objects for this thread/context. Serves
     * as a convenience method for accessing objects mapped to the
     * current thread.
     */
    val objects: MutableList<Any>
        // Retrieve the collection of objects mapped to this thread;
        // create an empty list if none is already created
        get() = threadObjects.getOrPut(Thread.currentThread()) { Collections.synchronizedList(mutableListOf()) }

    /**
     * Finds all instances of engine objects which extend the given type
     * `T`; multiple instances may be found, or an empty list if no
     * instances are found.
     *
     * @param T The thread-local collection of engine objects will be
     *     searched for objects extending this reified type.
     * @return Collection of all engine object instances which extend
     *     the given type parameter `T`.
     */
    inline fun <reified T> get(): List<T>
    {
        val list = mutableListOf<T>()

        // Perform linear search of all objects to collect requested type
        for (each in objects)
        {
            if (each::class.isSubclassOf(T::class))
                list += each as T
        }

        return list
    }

    /**
     * Creates and caches instances of all engine objects on the
     * classpath annotated with [RegisterObject].
     *
     * Also injects all instances of engine objects into peer engine
     * objects which [request them][EngineObject]. There is no guarantee
     * of what order objects are created or injected.
     */
    @JvmStatic
    fun initialize()
    {
        log.info("Scanning classpath for registered engine objects . . .")

        val annotationName = RegisterObject::class.java.name
        // Find all registered engine objects using classpath scanning
        val annotated = ClassGraph()
            .enableClassInfo()
            .enableAnnotationInfo()
            .scan(4)
            .getClassesWithAnnotation(annotationName)

        // Get current thread's engine object collection
        val objects = this.objects

        // Iterate through and load/instantiate each class and object
        for (each in annotated)
        {
            // Load the class into memory
            val eachClass = each.loadClass()
            log.debug("Found engine registration of class '${eachClass.simpleName}'")

            try
            {
                // Try to create using a zero-argument constructor
                val created = each.loadClass().getDeclaredConstructor().newInstance()
                // Cache the instance
                objects += created
            } catch (thrown: Throwable)
            {
                // The instance failed to instantiate
                log.error("Could not create instance of object via zero-argument constructor", thrown)
            }
        }

        log.debug("Placing cached object instances in annotated fields . . .")

        // Iterate through objects and inject all engine objects
        for (placeIn in objects)
            placeAll(placeIn)
    }

    /**
     * Injects all currently registered engine objects into the given
     * object in its [annotated fields][EngineObject].
     *
     * @param placeIn Object to search for annotated fields
     */
    fun placeAll(placeIn: Any)
    {
        // Iterate through thread's engine objects
        for (place in objects)
        {
            // Skip over self
            if (placeIn == place)
                continue

            // Place each instance in the annotated fields
            placeInstance(place, placeIn)
        }
    }

    /**
     * @return All member properties from the entire class hierarchy.
     */
    private val KClass<*>.allMemberProperties: List<KProperty1<*, *>>
        get()
        {
            // Get the lowest class' declared properties
            val returned = this.declaredMemberProperties.toMutableList()
            // Add all of the superclass' declared properties
            for (superClass in this.allSuperclasses)
                returned.addAll(superClass.declaredMemberProperties)

            return returned
        }

    /**
     * Searches through the given object for [annotated
     * fields][EngineObject] of the instance's type; injects the given
     * value into any fields that are found.
     *
     * @field instance Object instance to inject into annotated fields.
     * @field placeIn Object instance to search for annotated injection
     *      points.
     */
    @JvmStatic
    fun placeInstance(instance: Any, placeIn: Any)
    {
        // Scan all properties to find engine object injection points
        for (prop in placeIn::class.allMemberProperties)
            // Make sure has annotation and is right type (dirty)
            if (prop.hasAnnotation<EngineObject>() && prop.returnType.classifier is KClass<*>
                && instance::class.isSubclassOf(prop.returnType.classifier as KClass<*>))
            {
                // Set to accessible in case the field is private
                prop.isAccessible = true

                // Make sure the property isn't final
                if (prop is KMutableProperty<*>)
                    // Set the property value
                    prop.setter.call(placeIn, instance)
                else
                    log.error("Could not set annotated engine object; property is immutable")
            }
    }
}