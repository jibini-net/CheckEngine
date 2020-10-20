package net.jibini.check.engine.impl

import io.github.classgraph.ClassGraph
import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible

object EngineObjectsImpl
{
    private val log = LoggerFactory.getLogger(javaClass)

    private val threadObjects = ConcurrentHashMap<Thread, MutableList<Any>>()

    val objects: MutableList<Any>
        get() = threadObjects.getOrPut(Thread.currentThread()) { Collections.synchronizedList(mutableListOf()) }

    inline fun <reified T> get(): List<T>
    {
        val list = mutableListOf<T>()

        for (each in objects)
            if (each::class.isSubclassOf(T::class))
                list += each as T

        return list
    }

    @JvmStatic
    fun initialize()
    {
        log.info("Scanning classpath for registered engine objects . . .")

        val annotationName = RegisterObject::class.java.name

        val annotated = ClassGraph()
            .enableClassInfo()
            .enableAnnotationInfo()
            .scan(4)
            .getClassesWithAnnotation(annotationName)

        val objects = this.objects

        for (each in annotated)
        {
            val eachClass = each.loadClass()
            log.debug("Found engine registration of class '${eachClass.name}'")

            try
            {
                val created = each.loadClass().getDeclaredConstructor().newInstance()

                objects += created
            } catch (thrown: Throwable)
            {
                log.error("Could not create instance of object via default constructor", thrown)
            }
        }

        log.debug("Placing cached object instances in annotated fields . . .")

        for (place in objects)
            for (placeIn in objects)
            {
                if (placeIn == place)
                    continue

                placeInstance(place, placeIn)
            }
    }

    @JvmStatic
    fun placeInstance(instance: Any, placeIn: Any)
    {
        // Scan all properties to find engine object fields
        for (prop in placeIn::class.declaredMemberProperties)
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