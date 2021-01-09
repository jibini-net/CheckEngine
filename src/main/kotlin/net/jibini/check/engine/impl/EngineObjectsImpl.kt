package net.jibini.check.engine.impl

import io.github.classgraph.ClassGraph
import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

/**
 * Handles the creation and placement of annotated engine objects into annotated fields
 *
 * @author Zach Goethel
 */
object EngineObjectsImpl
{
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Collections of engine objects mapped to each thread
     */
    private val threadObjects = ConcurrentHashMap<Thread, MutableList<Any>>()

    /**
     * The collection of engine objects for this thread
     */
    val objects: MutableList<Any>
        // Get for the current thread on every reference
        get() = threadObjects.getOrPut(Thread.currentThread()) { Collections.synchronizedList(mutableListOf()) }

    /**
     * Gets a cached engine object of the given type parameter's class
     */
    inline fun <reified T> get(): List<T>
    {
        // Make empty list
        val list = mutableListOf<T>()

        // Iterate through objects
        for (each in objects)
            // Add to list if object is instance of wanted class type
            if (each::class.isSubclassOf(T::class))
                list += each as T

        return list
    }

    @JvmStatic
    fun initialize()
    {
        log.info("Scanning classpath for registered engine objects . . .")

        // Get registration annotation class name
        val annotationName = RegisterObject::class.java.name

        // Find all annotated classes using classpath scanning
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
            val eachClass = each.loadClass()
            log.debug("Found engine registration of class '${eachClass.simpleName}'")

            try
            {
                // Try to create using no-argument constructor
                val created = each.loadClass().getDeclaredConstructor().newInstance()

                objects += created
            } catch (thrown: Throwable)
            {
                log.error("Could not create instance of object via default constructor", thrown)
            }
        }

        log.debug("Placing cached object instances in annotated fields . . .")

        // Iterate through objects and place all objects in each object
        for (placeIn in objects)
            placeAll(placeIn)
    }

    /**
     * Places all currently registered engine objects into the given object in its annotated fields
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
     * Get all member properties from the entire class hierarchy
     */
    private val KClass<*>.allMemberProperties: List<KProperty1<*, *>>
        get()
        {
            val returned = this.declaredMemberProperties.toMutableList()

            for (superClass in this.allSuperclasses)
                returned.addAll(superClass.declaredMemberProperties)

            return returned
        }

    /**
     * Searches through the given object for annotated fields of the instance's type; fills in the annotated fields with
     * the given value
     *
     * @field instance Object instance to place in annotated fields
     * @field placeIn Object instance to search for annotated fields
     */
    @JvmStatic
    fun placeInstance(instance: Any, placeIn: Any)
    {
        // Scan all properties to find engine object fields
        for (prop in placeIn::class.allMemberProperties)
            // Make sure has annotation and is right type
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
                    log.error("Could not set annotated engine object; property is final")
            }
    }
}